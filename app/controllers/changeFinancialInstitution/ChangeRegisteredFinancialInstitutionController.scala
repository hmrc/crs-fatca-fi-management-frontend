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
import controllers.routes
import models.FinancialInstitutions.FIDetail
import models.UserAnswers
import models.requests.DataRequest
import pages.Page
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.ChangeUserAnswersRepository
import services.{FinancialInstitutionUpdateService, FinancialInstitutionsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CheckYourAnswersValidator, ContactHelper}
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
  retrieveCtUTR: CtUtrRetrievalAction,
  changeUserAnswersRepository: ChangeUserAnswersRepository,
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

  def onPageLoad(fiid: String): Action[AnyContent] = (identify andThen retrieveCtUTR() andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      financialInstitutionsService
        .getFinancialInstitution(request.fatcaId, fiid)
        .flatMap {
          case Some(fiDetails) =>
            userAnswers.get(ChangeFiDetailsInProgressId) match {
              case Some(id) if id.equalsIgnoreCase(fiid) => handleChangeInProgressFlow(fiid, userAnswers, fiDetails)(request)
              case _ =>
                financialInstitutionUpdateService
                  .populateAndSaveRegisteredFiDetails(userAnswers, fiDetails)
                  .map {
                    case (ua, fromChangedAnswers) if fromChangedAnswers =>
                      handleChangesInCacheFlow(fiDetails, ua)(request)
                    case (ua, fromChangedAnswers) =>
                      createPage(fiid, ua, hasChanges = fromChangedAnswers)
                  }
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

  private def handleChangeInProgressFlow(fiid: String, userAnswers: UserAnswers, fiDetails: FIDetail)(implicit request: DataRequest[AnyContent]) =
    getMissingAnswers(userAnswers) match {
      case Nil =>
        val hasChanges = financialInstitutionUpdateService.registeredFiDetailsHasChanged(userAnswers, fiDetails)
        Future.successful(createPage(fiid, userAnswers, hasChanges))
      case _ =>
        Future.successful(Redirect(routes.SomeInformationMissingController.onPageLoad()))
    }

  private def handleChangesInCacheFlow(fiDetail: FIDetail, ua: UserAnswers)(implicit request: DataRequest[AnyContent]) =
    ua.get(ReportForRegisteredBusinessPage) match {
      case Some(isFIUser) if isFIUser =>
        getMissingAnswers(ua) match {
          case Nil => createPage(fiDetail.FIID, ua, financialInstitutionUpdateService.registeredFiDetailsHasChanged(ua, fiDetail))
          case _   => Redirect(routes.SomeInformationMissingController.onPageLoad())
        }
      case _ => Redirect(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID))
    }

  def confirmAndAdd(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      getMissingAnswers(userAnswers) match {
        case Nil =>
          val fiName = getFinancialInstitutionName(userAnswers)
          financialInstitutionsService
            .updateFinancialInstitution(request.fatcaId, userAnswers)
            .flatMap(
              _ => financialInstitutionUpdateService.clearUserAnswers(userAnswers)
            )
            .flatMap(
              _ => changeUserAnswersRepository.clear(request.fatcaId, userAnswers.get(ChangeFiDetailsInProgressId))
            )
            .map(
              _ => Redirect(routes.DetailsUpdatedController.onPageLoad()).flashing("fiName" -> fiName)
            )
            .recoverWith {
              exception =>
                logger.error(s"Failed to clear user answers for subscription Id: [${request.fatcaId}]", exception)
                Future.successful(InternalServerError(errorView()))
            }
        case _ => Future.successful(Redirect(routes.SomeInformationMissingController.onPageLoad()))
      }
  }

  private def createPage(fiId: String, updatedUserAnswers: UserAnswers, hasChanges: Boolean)(implicit request: DataRequest[AnyContent]): Result = {
    val fiName                      = getFinancialInstitutionName(updatedUserAnswers)
    val financialInstitutionSummary = SummaryListViewModel(getChangeRegisteredFinancialInstitutionSummaries(fiId, updatedUserAnswers))
    Ok(view(hasChanges, fiName, financialInstitutionSummary))
  }

  private def getMissingAnswers(userAnswers: UserAnswers): Seq[Page] = CheckYourAnswersValidator(userAnswers).validateRegisteredBusiness

}
