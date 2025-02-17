/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.FIRemovedView

import java.time.{Clock, LocalDate, LocalTime}
import javax.inject.Inject

class FIRemovedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: FIRemovedView,
  val financialInstitutionsService: FinancialInstitutionsService,
  clock: Clock
) extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val ua   = request.userAnswers
      val fiId = "ABC00000122" // TODO: Replace placeholder FI ID with actual implementation as part of DAC6-3466
      val date = LocalDate.now(clock)
      val time = LocalTime.now(clock)

      Ok(view(getFinancialInstitutionName(ua), fiId, formatDate(date), formatTime(time)))
  }

}
