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

package controllers

import controllers.actions._
import forms.ContactNameFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{ContactNamePage, FirstContactEmailPage, NameOfFinancialInstitutionPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactNameView

import scala.concurrent.{ExecutionContext, Future}

class ContactNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ContactNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ContactNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  // TODO: Pass name of financial institution to view H1 when implemented

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val ua = request.userAnswers

      val preparedForm = ua.get(ContactNamePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      val fiName = ua.get(NameOfFinancialInstitutionPage)
      fiName match {
        case None       => Redirect(routes.IndexController.onPageLoad)
        case Some(name) => Ok(view(preparedForm, mode, name))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(NameOfFinancialInstitutionPage)
        .fold {
          Future.successful(Redirect(routes.IndexController.onPageLoad))
        } {
          name =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactNamePage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(ContactNamePage, mode, updatedAnswers))
              )
        }
  }

}
