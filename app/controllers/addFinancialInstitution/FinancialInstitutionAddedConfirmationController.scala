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
import models.UserAnswers
import pages.InformationSentPage
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{FinancialInstitutionAddedConfirmationView, ThereIsAProblemView}

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
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val fiId = "ABC00000122" // TODO: Replace placeholder FI ID with actual implementation when determined
      request.userAnswers.get(NameOfFinancialInstitutionPage) match {
        case Some(fiName) =>
          setInformationSentFlag(request.userAnswers).flatMap {
            case true => Future.successful(Ok(view(fiName, fiId)))
            case false =>
              logger.error(s"Failed to clear user answers after adding an FI for userId: [${request.userId}]")
              Future.successful(Ok(errorView()))
          }
        case None =>
          logger.error("Failed to get the name of financial institution from user answers")
          Future.successful(Ok(errorView()))
      }
  }

  private def setInformationSentFlag(userAnswers: UserAnswers): Future[Boolean] =
    Future.fromTry(userAnswers.set(InformationSentPage, true)).flatMap {
      updatedAnswers => sessionRepository.set(updatedAnswers)
    }

}
