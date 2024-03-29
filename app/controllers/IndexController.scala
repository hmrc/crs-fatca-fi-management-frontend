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

import controllers.actions.IdentifierAction
import models.UserAnswers
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  view: IndexView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async {
    implicit request =>
      sessionRepository.get(request.userId) flatMap {
        case Some(_) => Future.successful(Ok(view()))
        case None =>
          sessionRepository.set(UserAnswers(request.userId)) map {
            case true => Ok(view())
            case false =>
              logger.error(s"Failed to initialize user answers for userId: [${request.userId}]")
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

}
