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
import forms.addFinancialInstitution.SecondContactCanWePhoneFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.addFinancialInstitution.{NameOfFinancialInstitutionPage, SecondContactCanWePhonePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.addFinancialInstitution.SecondContactCanWePhoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondContactCanWePhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SecondContactCanWePhoneFormProvider,
  checkForInformationSentAction: CheckForInformationSentAction,
  val controllerComponents: MessagesControllerComponents,
  view: SecondContactCanWePhoneView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with ContactHelper
    with Logging
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSentAction) {
    implicit request =>
      val preparedForm = request.userAnswers.get(SecondContactCanWePhonePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      createResponse(
        (fiName, secondContactName) => Ok(view(preparedForm, mode, fiName, secondContactName))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              createResponse(
                (fiName, secondContactName) => BadRequest(view(formWithErrors, mode, fiName, secondContactName))
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondContactCanWePhonePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SecondContactCanWePhonePage, mode, updatedAnswers))
        )
  }

  private def createResponse(resultFunction: (String, String) => Result)(implicit request: DataRequest[AnyContent]): Result =
    request.userAnswers.get(NameOfFinancialInstitutionPage) match {
      case Some(financialInstitutionName) => resultFunction(financialInstitutionName, getSecondContactName(request.userAnswers))
      case None =>
        logger.error("Failed to get name of financial institution")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }

}
