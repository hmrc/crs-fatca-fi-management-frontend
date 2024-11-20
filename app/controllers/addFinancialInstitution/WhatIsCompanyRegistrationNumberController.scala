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

import forms.addFinancialInstitution.CompanyRegistrationNumberFormProvider
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.addFinancialInstitution.WhatIsCompanyRegistrationNumberView

import javax.inject.Inject

class WhatIsCompanyRegistrationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  formProvider: CompanyRegistrationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsCompanyRegistrationNumberView
) extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = Action {
    implicit request =>
      Ok(view(form, mode, "Financial Institution"))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = Action {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, mode, "Financial Institution")),
          _ => Redirect(controllers.addFinancialInstitution.routes.WhatIsCompanyRegistrationNumberController.onPageLoad().url)
        )
  }

}
