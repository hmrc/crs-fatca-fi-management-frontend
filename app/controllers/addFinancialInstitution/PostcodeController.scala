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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.addFinancialInstitution.PostcodeFormProvider
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.{AddressLookupPage, PostcodePage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.addFinancialInstitution.PostcodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostcodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: PostcodeFormProvider,
  addressLookupConnector: AddressLookupConnector,
  val controllerComponents: MessagesControllerComponents,
  view: PostcodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(PostcodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getFinancialInstitutionName(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val formReturned = form.bindFromRequest()

      formReturned
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, getFinancialInstitutionName(request.userAnswers)))),
          postCode =>
            addressLookupConnector.addressLookupByPostcode(postCode).flatMap {
              case Nil =>
                val formError = formReturned.withError(FormError("postCode", List("postcode.error.notFound")))
                Future.successful(BadRequest(view(formError, mode, getFinancialInstitutionName(request.userAnswers))))

              case addresses =>
                for {
                  updatedAnswers            <- Future.fromTry(request.userAnswers.set(PostcodePage, postCode))
                  updatedAnswersWithAddress <- Future.fromTry(updatedAnswers.set(AddressLookupPage, addresses))
                  _                         <- sessionRepository.set(updatedAnswersWithAddress)
                } yield Redirect(navigator.nextPage(PostcodePage, mode, updatedAnswersWithAddress))
            }
        )
  }

}
