/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.changeFinancialInstitution

import com.google.inject.Inject
import controllers.actions._
import models.requests.DataRequest
import models.{ChangeAnswers, UserAnswers}
import pages.Page
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{FinancialInstitutionUpdateService, FinancialInstitutionsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CheckYourAnswersValidator, ContactHelper}
import viewmodels.changeFinancialInstitution.ChangeFinancialInstitutionViewModel.getChangeFinancialInstitutionSummaries
import viewmodels.common.{getFirstContactSummaries, getSecondContactSummaries}
import viewmodels.govuk.summarylist._
import views.html.ThereIsAProblemView
import views.html.changeFinancialInstitution.ChangeFinancialInstitutionView

import scala.concurrent.{ExecutionContext, Future}

class ChangeFinancialInstitutionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  financialInstitutionsService: FinancialInstitutionsService,
  financialInstitutionUpdateService: FinancialInstitutionUpdateService,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeFinancialInstitutionView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with ContactHelper
    with I18nSupport
    with Logging {

  def onPageLoad(fiid: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      financialInstitutionsService
        .getFinancialInstitution(request.fatcaId, fiid)
        .flatMap {
          case Some(fiDetails) =>
            userAnswers.get(ChangeFiDetailsInProgressId) match {
              case Some(id) if id.equalsIgnoreCase(fiid) =>
                getMissingAnswers(userAnswers) match {
                  case Nil =>
                    val hasChanges = financialInstitutionUpdateService.fiDetailsHasChanged(userAnswers, fiDetails)
                    Future.successful(createPage(fiid, userAnswers, hasChanges))
                  case _ =>
                    Future.successful(Redirect(controllers.routes.SomeInformationMissingController.onPageLoad()))
                }
              case _ =>
                financialInstitutionUpdateService
                  .populateAndSaveFiDetails(userAnswers, fiDetails)
                  .map(createPage(fiid, _, hasChanges = false))
                  .recoverWith {
                    exception =>
                      logger.error(s"Failed to populate and save FI details to user answers for subscription Id: [${request.fatcaId}] and FI Id [$fiid]",
                                   exception
                      )
                      Future.successful(InternalServerError(errorView()))
                  }
            }
          case _ =>
            logger.error(s"Failed to retrieve FI details from backend for subscription Id: [${request.fatcaId}] and FI Id [$fiid]")
            Future.successful(InternalServerError(errorView()))
        }
        .recoverWith {
          exception =>
            logger.error(s"Failed to get FI details for subscription Id: [${request.fatcaId}] and FI Id [$fiid]", exception)
            Future.successful(InternalServerError(errorView()))
        }
  }

  def confirmAndAdd(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val fiName = getFinancialInstitutionName(request.userAnswers)
      financialInstitutionsService
        .updateFinancialInstitution(request.fatcaId, request.userAnswers)
        .flatMap(
          _ => financialInstitutionUpdateService.clearUserAnswers(request.userAnswers)
        )
        .map(
          _ => Redirect(controllers.routes.DetailsUpdatedController.onPageLoad(fiName))
        )
        .recoverWith {
          exception =>
            logger.error(s"Failed to clear user answers for subscription Id: [${request.fatcaId}]", exception)
            Future.successful(InternalServerError(errorView()))
        }
  }

  private def createPage(fiId: String, updatedUserAnswers: UserAnswers, hasChanges: Boolean)(implicit request: DataRequest[AnyContent]): Result = {
    val fiName                      = getFinancialInstitutionName(updatedUserAnswers)
    val financialInstitutionSummary = SummaryListViewModel(getChangeFinancialInstitutionSummaries(fiId, updatedUserAnswers))
    val firstContactSummary         = SummaryListViewModel(getFirstContactSummaries(updatedUserAnswers, ChangeAnswers))
    val secondContactSummary        = SummaryListViewModel(getSecondContactSummaries(updatedUserAnswers, ChangeAnswers))
    Ok(view(hasChanges, fiName, financialInstitutionSummary, firstContactSummary, secondContactSummary))
  }

  private def getMissingAnswers(userAnswers: UserAnswers): Seq[Page] = CheckYourAnswersValidator(userAnswers).validate

}
