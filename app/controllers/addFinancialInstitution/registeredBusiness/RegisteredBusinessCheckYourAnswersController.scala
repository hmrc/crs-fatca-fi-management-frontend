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

package controllers.addFinancialInstitution.registeredBusiness

import com.google.inject.Inject
import controllers.actions._
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import viewmodels.checkAnswers.CheckYourAnswersViewModel._
import viewmodels.govuk.summarylist._
import views.html.addFinancialInstitution.IsRegisteredBusiness.RegisteredBusinessCheckYourAnswersView

class RegisteredBusinessCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkForInformationSent: CheckForInformationSentAction,
  val controllerComponents: MessagesControllerComponents,
  view: RegisteredBusinessCheckYourAnswersView
) extends FrontendBaseController
    with ContactHelper
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent) {
    implicit request =>
      val ua: UserAnswers          = request.userAnswers
      val fiName                   = getFinancialInstitutionName(ua)
      val financialInstitutionList = SummaryListViewModel(getRegisteredBusinessSummaries(ua))

      Ok(view(fiName, financialInstitutionList))
  }

  def confirmAndAdd(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSent) {
    Redirect(routes.RegisteredBusinessCheckYourAnswersController.onPageLoad())
  }

}