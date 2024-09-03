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
import forms.RemoveAreYouSureFormProvider
import navigation.Navigator
import pages.RemoveAreYouSurePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveAreYouSureView

import javax.inject.Inject

class RemoveAreYouSureController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveAreYouSureFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAreYouSureView
) extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean]   = formProvider()
  private val placeholderId = "ABC00000122"

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(RemoveAreYouSurePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, placeholderId))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, placeholderId)),
          value => Redirect(navigator.removeNavigation(value))
        )
  }

}