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

import controllers.actions.{CtUtrRetrievalAction, IdentifierAction}
import models.requests.IdentifierRequest
import models.{NormalMode, UserAnswers}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddFIController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  retrieveCtUTR: CtUtrRetrievalAction,
  financialInstitutionsService: FinancialInstitutionsService,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen retrieveCtUTR()).async {
    implicit request =>
      for {
        institutions <- financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId)
        redirectCall <- redirectUrl(request.autoMatched, request.userType, institutions.nonEmpty)
      } yield Redirect(redirectCall)
  }

  private def redirectUrl(autoMatched: Boolean, affinityGroup: AffinityGroup, hasSomeFIs: Boolean)(implicit request: IdentifierRequest[_]): Future[Call] =
    (autoMatched, affinityGroup, hasSomeFIs) match {
      case (true, Organisation, false) =>
        for {
          _ <- ensureSession(request.userId)
          _ <- ensureSession(request.fatcaId)
        } yield controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode)
      case _ =>
        Future.successful(routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode))
    }

  private def ensureSession(userId: String): Future[Unit] =
    sessionRepository.get(userId).flatMap {
      case Some(_) => Future.successful(())
      case None =>
        sessionRepository
          .set(UserAnswers(userId))
          .map(
            _ => ()
          )
    }

}
