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

package controllers.addFinancialInstitution

import com.google.inject.Inject
import controllers.actions._
import models.{CheckAnswers, UserAnswers}
import pages.{FiidPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CheckYourAnswersValidator, ContactHelper}
import viewmodels.checkAnswers.CheckYourAnswersViewModel._
import viewmodels.common.{getFirstContactSummaries, getSecondContactSummaries}
import viewmodels.govuk.summarylist._
import views.html.addFinancialInstitution.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkForInformationSent: CheckForInformationSentAction,
  service: FinancialInstitutionsService,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  changeUserAnswersRepository: ChangeUserAnswersRepository,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with ContactHelper
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent) {
    implicit request =>
      val ua: UserAnswers          = request.userAnswers
      val fiName                   = getFinancialInstitutionName(ua)
      val financialInstitutionList = SummaryListViewModel(getFinancialInstitutionSummaries(ua))
      val firstContactList         = SummaryListViewModel(getFirstContactSummaries(ua, CheckAnswers))
      val secondContactList        = SummaryListViewModel(getSecondContactSummaries(ua, CheckAnswers))

      getMissingAnswers(ua) match {
        case Nil => Ok(view(fiName, financialInstitutionList, firstContactList, secondContactList))
        case _   => Redirect(controllers.routes.SomeInformationMissingController.onPageLoad())
      }
  }

  def confirmAndAdd(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent).async {
    implicit request =>
      service
        .addFinancialInstitution(request.fatcaId, request.userAnswers)
        .flatMap(
          resp => Future.fromTry(request.userAnswers.set(FiidPage, resp.fiid.get))
        )
        .flatMap(sessionRepository.set)
        .flatMap(
          _ => changeUserAnswersRepository.clear(request.fatcaId)
        )
        .map {
          _ =>
            Redirect(controllers.addFinancialInstitution.routes.FinancialInstitutionAddedConfirmationController.onPageLoad)
        }
        .recover {
          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  private def getMissingAnswers(userAnswers: UserAnswers): Seq[Page] = CheckYourAnswersValidator(userAnswers).validate

}
