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
import forms.SecondContactEmailFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{ContactNamePage, SecondContactEmailPage, SecondContactNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SecondContactEmailView

import scala.concurrent.{ExecutionContext, Future}

class SecondContactEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SecondContactEmailFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SecondContactEmailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()
  val fi   = "Placeholder Financial Institution" // todo: pull in this when available

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val ua = request.userAnswers
      val preparedForm = ua.get(SecondContactEmailPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val secondContactName = ua.get(SecondContactNamePage)
      secondContactName match {
        case None       => Redirect(routes.IndexController.onPageLoad)
        case Some(name) => Ok(view(preparedForm, mode, fi, name))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(SecondContactNamePage)
        .fold {
          Future.successful(Redirect(routes.IndexController.onPageLoad))
        } {
          name =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fi, name))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondContactEmailPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(SecondContactEmailPage, mode, updatedAnswers))
              )
        }
  }

}
