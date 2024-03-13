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
import forms.InstitutionSelectAddressFormProvider

import javax.inject.Inject
import models.{AddressLookup, Mode}
import navigation.Navigator
import pages.{AddressLookupPage, InstitutionSelectAddressPage, InstitutionSelectedAddressLookupPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.InstitutionSelectAddressView

import scala.concurrent.{ExecutionContext, Future}

class InstitutionSelectAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: InstitutionSelectAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: InstitutionSelectAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(AddressLookupPage) match {
        case Some(addresses) =>
          val preparedForm: Form[String] = request.userAnswers.get(InstitutionSelectAddressPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          val addressOptions: Seq[RadioItem] = addresses.map(
            address => RadioItem(content = Text(s"${formatAddress(address)}"), value = Some(s"${formatAddress(address)}"))
          )

          Ok(view(preparedForm, addressOptions, mode))

        case None => Redirect(routes.InstitutionUkAddressController.onPageLoad(mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(AddressLookupPage) match {
        case Some(addresses) =>
          val radios: Seq[RadioItem] = addresses.map(
            address => RadioItem(content = Text(s"${formatAddress(address)}"), value = Some(s"${formatAddress(address)}"))
          )

          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, radios, mode))),
              value => {
                val addressToStore: AddressLookup = addresses.find(formatAddress(_) == value).getOrElse(throw new Exception("Cannot get address"))

                for {
                  updatedAnswers                    <- Future.fromTry(request.userAnswers.set(InstitutionSelectAddressPage, value))
                  updatedAnswersWithSelectedAddress <- Future.fromTry(updatedAnswers.set(InstitutionSelectedAddressLookupPage, addressToStore))
                  _                                 <- sessionRepository.set(updatedAnswersWithSelectedAddress)
                } yield Redirect(navigator.nextPage(InstitutionSelectAddressPage, mode, updatedAnswersWithSelectedAddress))
              }
            )

        case None => Future.successful(Redirect(routes.InstitutionUkAddressController.onPageLoad(mode)))
      }
  }

  private def formatAddress(address: AddressLookup): String = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4).flatten.mkString(", ")
    val county = address.county.fold("")(
      county => s"$county, "
    )

    s"$lines, ${address.town}, $county${address.postcode}"
  }

}
