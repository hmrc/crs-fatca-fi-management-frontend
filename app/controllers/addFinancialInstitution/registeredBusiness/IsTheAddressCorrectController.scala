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

import controllers.actions._
import controllers.routes
import forms.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectFormProvider
import models.{AddressResponse, Mode}
import navigation.Navigator
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, IsTheAddressCorrectPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.RegistrationWithUtrService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ContactHelper, CountryListFactory}
import views.html.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class IsTheAddressCorrectController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IsTheAddressCorrectFormProvider,
  regService: RegistrationWithUtrService,
  retrieveCtUTR: CtUtrRetrievalAction,
  countryListFactory: CountryListFactory,
  val controllerComponents: MessagesControllerComponents,
  view: IsTheAddressCorrectView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen retrieveCtUTR() andThen getData andThen requireData).async {
    implicit request =>
      request.ctutr match {
        case Some(utr) =>
          regService
            .fetchAddress(utr)
            .flatMap {
              address =>
                for {
                  addressWithCountry <- Future.fromTry(addCountryToAddress(address))
                  updatedAnswers     <- Future.fromTry(request.userAnswers.set(FetchedRegisteredAddressPage, addressWithCountry))
                  result <- sessionRepository.set(updatedAnswers).map {
                    _ =>
                      val preparedForm = request.userAnswers.get(IsTheAddressCorrectPage) match {
                        case None        => form
                        case Some(value) => form.fill(value)
                      }
                      Ok(view(preparedForm, mode, getFinancialInstitutionName(request.userAnswers), addressWithCountry))
                  }
                } yield result

            }
            .recoverWith {
              case e =>
                logger.error(s"Failed to fetch address for UTR $utr", e)
                Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            }
        case None =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }

  }

  private def addCountryToAddress(addressResponse: AddressResponse): Try[AddressResponse] =
    if (addressResponse.country.isDefined) {
      Success(addressResponse)
    } else {
      countryListFactory.findCountryWithCode(addressResponse.countryCode) match {
        case Some(country) =>
          Success(addressResponse.copy(countryCode = country.description))
        case None =>
          logger.error(s"Country with code ${addressResponse.countryCode} not found in list of countries")
          Failure(new RuntimeException("Country not found"))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(FetchedRegisteredAddressPage)
        .fold {
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        } {
          address =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future
                    .successful(BadRequest(view(formWithErrors, mode, getFinancialInstitutionName(request.userAnswers), address))),
                value =>
                  for {

                    updatedAnswers <- Future.fromTry(request.userAnswers.set(IsTheAddressCorrectPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(IsTheAddressCorrectPage, mode, updatedAnswers))
              )
        }
  }

}
