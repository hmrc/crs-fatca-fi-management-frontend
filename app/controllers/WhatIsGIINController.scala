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
import forms.WhatIsGIINFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{HaveGIINPage, WhatIsGIINPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.WhatIsGIINView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsGIINController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatIsGIINFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsGIINView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhatIsGIINPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val haveGIINAnswered = request.userAnswers.get(HaveGIINPage) match {
        case None        => true
        case Some(value) => false
      }

      Ok(view(preparedForm, mode, getFinancialInstitutionName(request.userAnswers), haveGIINAnswered))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val haveGIINAnswered = request.userAnswers.get(HaveGIINPage) match {
        case None        => true
        case Some(value) => false
      }
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, getFinancialInstitutionName(request.userAnswers), haveGIINAnswered))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsGIINPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatIsGIINPage, mode, updatedAnswers))
        )
  }

}
