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
import connectors.AddressLookupConnector
import forms.addFinancialInstitution.PostcodeFormProvider
import models.{AddressLookup, Country, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.addFinancialInstitution.{NameOfFinancialInstitutionPage, PostcodePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.addFinancialInstitution.PostcodeView

import scala.concurrent.Future

class PostcodeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PostcodeFormProvider()
  val form         = formProvider()
  val contactName  = "fiName"
  private val ua   = emptyUserAnswers.set(NameOfFinancialInstitutionPage, contactName).get

  lazy val PostcodeRoute = routes.PostcodeController.onPageLoad(NormalMode).url

  "InstitutionPostcode Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, PostcodeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PostcodeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "fiName")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(PostcodePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, PostcodeRoute)

        val view = application.injector.instanceOf[PostcodeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, "fiName")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository: SessionRepository = mock[SessionRepository]
      val mockAddressLookupConnector               = mock[AddressLookupConnector]

      val addresses: Seq[AddressLookup] = Seq(
        AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ", Some(Country.GB)),
        AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ", None)
      )

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupConnector.addressLookupByPostcode(any[String])(any(), any()))
        .thenReturn(Future.successful(addresses))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, PostcodeRoute)
            .withFormUrlEncodedBody(("postCode", "AA1 1AA"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockAddressLookupConnector, times(1)).addressLookupByPostcode(any())(any(), any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, PostcodeRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PostcodeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "fiName")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, PostcodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, PostcodeRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
