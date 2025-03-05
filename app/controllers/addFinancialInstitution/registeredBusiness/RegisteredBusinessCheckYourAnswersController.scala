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

package controllers.addFinancialInstitution.registeredBusiness

import com.google.inject.Inject
import controllers.actions._
import controllers.routes
import models.UserAnswers
import pages.{FiidPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CheckYourAnswersValidator, ContactHelper}
import viewmodels.checkAnswers.CheckYourAnswersViewModel._
import viewmodels.govuk.summarylist._
import views.html.addFinancialInstitution.IsRegisteredBusiness.RegisteredBusinessCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RegisteredBusinessCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkForInformationSent: CheckForInformationSentAction,
  val controllerComponents: MessagesControllerComponents,
  val financialInstitutionsService: FinancialInstitutionsService,
  sessionRepository: SessionRepository,
  view: RegisteredBusinessCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with ContactHelper
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent) {
    implicit request =>
      val ua: UserAnswers          = request.userAnswers
      val fiName                   = getFinancialInstitutionName(ua)
      val financialInstitutionList = SummaryListViewModel(getRegisteredBusinessSummaries(ua))

      getMissingAnswers(ua) match {
        case Nil => Ok(view(fiName, financialInstitutionList))
        case _   => Redirect(routes.SomeInformationMissingController.onPageLoad())
      }
  }

  def confirmAndAdd(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent).async {
    implicit request =>
      financialInstitutionsService
        .addFinancialInstitution(request.fatcaId, request.userAnswers)
        .flatMap(
          resp => Future.fromTry(request.userAnswers.set(FiidPage, resp.fiid.get))
        )
        .flatMap(sessionRepository.set)
        .map(
          _ => Redirect(controllers.addFinancialInstitution.routes.FinancialInstitutionAddedConfirmationController.onPageLoad)
        )
  }

  private def getMissingAnswers(userAnswers: UserAnswers): Seq[Page] = CheckYourAnswersValidator(userAnswers).validateRegisteredBusiness

}
