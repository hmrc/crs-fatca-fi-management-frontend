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

import controllers.actions._
import forms.addFinancialInstitution.WhichIdentificationNumbersFormProvider
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.WhichIdentificationNumbersPage
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.addFinancialInstitution.WhichIdentificationNumbersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhichIdentificationNumbersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  changeUserAnswersRepository: ChangeUserAnswersRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkForInformationSent: CheckForInformationSentAction,
  formProvider: WhichIdentificationNumbersFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhichIdentificationNumbersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with ContactHelper
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent) {
    implicit request =>
      val fiName = getFinancialInstitutionName(request.userAnswers)
      val preparedForm = request.userAnswers.get(WhichIdentificationNumbersPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(fiName, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val fiName = getFinancialInstitutionName(request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(fiName, formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhichIdentificationNumbersPage, value))
              cleanedAnswers <- Future.fromTry(
                WhichIdentificationNumbersPage.cleanUpUnselectedTINPages(
                  selectedTINs = value,
                  userAnswers = updatedAnswers
                )
              )
              _ <- sessionRepository.set(cleanedAnswers)
              _ <- changeUserAnswersRepository.set(request.fatcaId, cleanedAnswers.get(ChangeFiDetailsInProgressId), cleanedAnswers)
            } yield Redirect(navigator.nextPage(WhichIdentificationNumbersPage, mode, cleanedAnswers))
        )
  }

}
