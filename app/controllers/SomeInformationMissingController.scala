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
import models.NormalMode
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersValidator
import views.html.SomeInformationMissingView

import javax.inject.Inject

class SomeInformationMissingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SomeInformationMissingView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      def getStandardFICheckAnswers = {
        val redirect = CheckYourAnswersValidator(request.userAnswers).changeAnswersRedirectUrl
        Ok(view(redirect))
      }

      request.userAnswers.get(ChangeFiDetailsInProgressId) match {
        case None =>
          if (request.userAnswers.get(ReportForRegisteredBusinessPage).isEmpty) {
            Ok(view(controllers.addFinancialInstitution.routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url))
          } else {
            Ok(view(controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode).url))
          }
        case Some(_) =>
          request.userAnswers.get(ReportForRegisteredBusinessPage) match {
            case Some(value) =>
              if (value) {
                val redirect = CheckYourAnswersValidator(request.userAnswers).changeAnswersRedirectUrlForRegisteredBusiness
                Ok(view(redirect))
              } else {
                getStandardFICheckAnswers
              }
            case None => getStandardFICheckAnswers
          }
      }
  }

}
