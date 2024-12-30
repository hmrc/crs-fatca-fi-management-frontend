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
import forms.addFinancialInstitution.IsThisAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.{AddressLookupPage, IsThisAddressPage, SelectedAddressLookupPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.addFinancialInstitution.IsThisAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IsThisAddressFormProvider,
  checkForInformationSentAction: CheckForInformationSentAction,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSentAction) {
    implicit request =>
      val ua = request.userAnswers
      ua.get(AddressLookupPage) match {
        case Some(address) =>
          val preparedForm = ua.get(IsThisAddressPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode, getFinancialInstitutionName(ua), address.head.toAddress))

        case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ua = request.userAnswers
      ua.get(AddressLookupPage) match {
        case Some(address) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, getFinancialInstitutionName(request.userAnswers), address.head.toAddress))),
              value =>
                for {
                  updatedAnswers                    <- Future.fromTry(ua.set(IsThisAddressPage, value))
                  updatedAnswersWithSelectedAddress <- Future.fromTry(updatedAnswers.set(SelectedAddressLookupPage, address.head))
                  _                                 <- sessionRepository.set(updatedAnswersWithSelectedAddress)
                } yield Redirect(navigator.nextPage(IsThisAddressPage, mode, updatedAnswersWithSelectedAddress))
            )
        case None => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      }
  }

}
