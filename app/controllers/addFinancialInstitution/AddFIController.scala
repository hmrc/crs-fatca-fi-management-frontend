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
import models.NormalMode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.Future

class AddFIController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  retrieveCtUTR: CtUtrRetrievalAction
) extends FrontendBaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen retrieveCtUTR()).async {
    implicit request =>
      Future.successful(Redirect(redirectUrl(request.autoMatched, request.userType)))
  }

  private def redirectUrl(autoMatched: Boolean, affinityGroup: AffinityGroup) =
    (autoMatched, affinityGroup) match {
      case (true, Organisation) =>
        controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode)
      case _ =>
        routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
    }

}
