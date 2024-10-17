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
import models.UserAnswers
import models.requests.DataRequest
import pages.changeFinancialInstitution.ChangeRegisteredFiDetailsInProgressId
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{FinancialInstitutionUpdateService, FinancialInstitutionsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import viewmodels.changeFinancialInstitution.ChangeRegisteredFinancialInstitutionViewModel.getChangeRegisteredFinancialInstitutionSummaries
import viewmodels.govuk.summarylist._
import views.html.ThereIsAProblemView
import views.html.changeFinancialInstitution.ChangeRegisteredFinancialInstitutionView

import scala.concurrent.{ExecutionContext, Future}

class ChangeRegisteredFinancialInstitutionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  financialInstitutionsService: FinancialInstitutionsService,
  financialInstitutionUpdateService: FinancialInstitutionUpdateService,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeRegisteredFinancialInstitutionView,
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
            userAnswers.get(ChangeRegisteredFiDetailsInProgressId) match {
              case Some(id) if id.equalsIgnoreCase(fiid) =>
                val hasChanges = financialInstitutionUpdateService.registeredFiDetailsHasChanged(userAnswers, fiDetails)
                Future.successful(createPage(fiid, userAnswers, hasChanges))
              case _ =>
                financialInstitutionUpdateService
                  .populateAndSaveRegisteredFiDetails(userAnswers, fiDetails)
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
      financialInstitutionUpdateService
        .clearUserAnswers(request.userAnswers)
        .map(
          _ => // TODO: User answers to be submitted and redirected to /details-updated as part of DAC6-3186
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        )
        .recoverWith {
          exception =>
            logger.error(s"Failed to clear user answers for subscription Id: [${request.fatcaId}]", exception)
            Future.successful(InternalServerError(errorView()))
        }
  }

  private def createPage(fiId: String, updatedUserAnswers: UserAnswers, hasChanges: Boolean)(implicit request: DataRequest[AnyContent]): Result = {
    val fiName                      = getFinancialInstitutionName(updatedUserAnswers)
    val financialInstitutionSummary = SummaryListViewModel(getChangeRegisteredFinancialInstitutionSummaries(fiId, updatedUserAnswers))
    Ok(view(hasChanges, fiName, financialInstitutionSummary))
  }

}
