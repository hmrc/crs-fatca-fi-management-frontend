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
import pages.FiidPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.{FinancialInstitutionAddedConfirmationView, PageUnavailableView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionAddedConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: FinancialInstitutionAddedConfirmationView,
  errorView: PageUnavailableView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with ContactHelper
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ua = request.userAnswers
      ua.get(FiidPage) match {
        case None => Future.successful(Ok(errorView(controllers.routes.IndexController.onPageLoad().url)))
        case Some(fiIdValue) =>
          val fiName = getFinancialInstitutionName(ua)
          sessionRepository.set(ua.copy(data = Json.obj())).map {
            case true => Ok(view(fiName, fiIdValue))
            case false =>
              logger.error(s"Failed to clear user answers after adding an FI for userId: [${request.userId}]")
              Ok(errorView(controllers.routes.IndexController.onPageLoad().url))

          }
      }
  }

}
