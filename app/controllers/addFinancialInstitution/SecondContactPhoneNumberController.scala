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
import forms.addFinancialInstitution.SecondContactPhoneNumberFormProvider
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.{SecondContactNamePage, SecondContactPhoneNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.addFinancialInstitution.SecondContactPhoneNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondContactPhoneNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SecondContactPhoneNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SecondContactPhoneNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val contactName = request.userAnswers.get(SecondContactNamePage).getOrElse("the second contact")
      // should it kick out if therre is a problem getting SecondContactNamePage?
      val preparedForm = request.userAnswers.get(SecondContactPhoneNumberPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(contactName, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val contactName = request.userAnswers.get(SecondContactNamePage).getOrElse("Second Contact")
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(contactName, formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondContactPhoneNumberPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SecondContactPhoneNumberPage, mode, updatedAnswers))
        )
  }

}
