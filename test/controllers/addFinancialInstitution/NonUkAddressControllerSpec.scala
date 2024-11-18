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

import base.SpecBase
import config.FrontendAppConfig
import forms.addFinancialInstitution.NonUkAddressFormProvider
import models.{Address, Country, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.addFinancialInstitution.{NameOfFinancialInstitutionPage, NonUkAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.CountryListFactory
import views.html.addFinancialInstitution.NonUkAddressView

import scala.concurrent.Future

class NonUkAddressControllerSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {

  private val PostCode     = "XX9 9XX"
  private val AddressLine1 = "value 1"
  private val AddressLine2 = "value 2"
  private val AddressLine3 = "value 3"
  private val AddressLine4 = "value 4"

  private val testCountry: Country             = Country("valid", "AG", "Antigua and Barbuda")
  private val testCountryList: Seq[Country]    = Seq(testCountry)
  private val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  private val address: Address = Address(AddressLine1, Some(AddressLine2), AddressLine3, Some(AddressLine4), Some(PostCode))

  private val countryListFactory: CountryListFactory = new CountryListFactory(app.environment, mockAppConfig) {
    override lazy val countryList: Seq[Country] = testCountryList
  }

  private val formProvider = new NonUkAddressFormProvider()
  private val form         = formProvider(testCountryList)

  private def onwardRoute                    = Call("GET", "/foo")
  private lazy val nonUkAddressRoute: String = routes.NonUkAddressController.onPageLoad(NormalMode).url

  "NonUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CountryListFactory].to(countryListFactory))
        .build()

      running(application) {
        val request = FakeRequest(GET, nonUkAddressRoute)
        val view    = application.injector.instanceOf[NonUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          countryListFactory.countrySelectList(form.data, testCountryList),
          fiName,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .withPage(NameOfFinancialInstitutionPage, fiName)
        .withPage(NonUkAddressPage, address)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CountryListFactory].to(countryListFactory))
        .build()

      running(application) {
        val request = FakeRequest(GET, nonUkAddressRoute)
        val view    = application.injector.instanceOf[NonUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(address),
          countryListFactory.countrySelectList(form.data, testCountryList),
          fiName,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CountryListFactory].to(countryListFactory)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonUkAddressRoute)
            .withFormUrlEncodedBody(
              ("addressLine1", AddressLine1),
              ("addressLine2", AddressLine2),
              ("addressLine3", AddressLine2),
              ("addressLine4", AddressLine2),
              ("postCode", PostCode),
              ("country", testCountry.code)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and corresponding view when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CountryListFactory].to(countryListFactory))
        .build()

      running(application) {
        val request   = FakeRequest(POST, nonUkAddressRoute).withFormUrlEncodedBody(("value", "invalid data"))
        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[NonUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          countryListFactory.countrySelectList(form.data, testCountryList),
          fiName,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[CountryListFactory].to(countryListFactory))
        .build()

      running(application) {
        val request = FakeRequest(GET, nonUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[CountryListFactory].to(countryListFactory))
        .build()

      running(application) {
        val request = FakeRequest(POST, nonUkAddressRoute).withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
