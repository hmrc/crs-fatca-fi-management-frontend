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
import forms.addFinancialInstitution._
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.{FirstContactHavePhonePage, FirstContactNamePage}
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.addFinancialInstitution.FirstContactHavePhoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FirstContactHavePhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  changeUserAnswersRepository: ChangeUserAnswersRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: FirstContactHavePhoneFormProvider,
  checkForInformationSentAction: CheckForInformationSentAction,
  val controllerComponents: MessagesControllerComponents,
  view: FirstContactHavePhoneView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSentAction) {
    implicit request =>
      val ua          = request.userAnswers
      val contactName = ua.get(FirstContactNamePage)
      val fi          = getFinancialInstitutionName(ua)

      val preparedForm = ua.get(FirstContactHavePhonePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      contactName match {
        case None              => Redirect(controllers.routes.IndexController.onPageLoad())
        case Some(contactName) => Ok(view(preparedForm, mode, fi, contactName))

      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ua = request.userAnswers
      val fi = getFinancialInstitutionName(ua)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fi, getFirstContactName(ua)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(ua.set(FirstContactHavePhonePage, value))
              _              <- sessionRepository.set(updatedAnswers)
              _              <- changeUserAnswersRepository.set(request.fatcaId, updatedAnswers.get(ChangeFiDetailsInProgressId), updatedAnswers)
            } yield Redirect(navigator.nextPage(FirstContactHavePhonePage, mode, updatedAnswers))
        )
  }

}
