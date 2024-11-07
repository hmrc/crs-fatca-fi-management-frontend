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
import forms.addFinancialInstitution.UkAddressFormProvider
import models.{Address, Country, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.addFinancialInstitution.{NameOfFinancialInstitutionPage, UkAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.addFinancialInstitution.UkAddressView

import scala.concurrent.Future

class UkAddressControllerSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val address: Address = Address("value 1", Some("value 2"), "value 3", Some("value 4"), Some("XX9 9XX"), Country.GB)

  private val formProvider = new UkAddressFormProvider()
  private val form         = formProvider()

  private lazy val ukAddressRoute: String = routes.UkAddressController.onPageLoad(NormalMode).url

  "UkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          fiName,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .withPage(NameOfFinancialInstitutionPage, fiName)
        .withPage(UkAddressPage, address)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val view = application.injector.instanceOf[UkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(address),
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
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(
              ("addressLine1", "value 1"),
              ("addressLine2", "value 2"),
              ("addressLine3", "value 2"),
              ("addressLine4", "value 2"),
              ("postCode", "XX9 9XX"),
              ("country", "GG")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and corresponding view when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid data"))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          fiName,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request =
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
