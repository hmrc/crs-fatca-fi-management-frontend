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

import base.SpecBase
import forms.InstitutionSelectAddressFormProvider
import models.{AddressLookup, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AddressLookupPage, NameOfFinancialInstitutionPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.InstitutionSelectAddressView

import scala.concurrent.Future

class InstitutionSelectAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val institutionSelectAddressRoute = routes.InstitutionSelectAddressController.onPageLoad(NormalMode).url

  val formProvider = new InstitutionSelectAddressFormProvider()
  val form         = formProvider()

  val contactName = "fiName"
  private val ua  = emptyUserAnswers.set(NameOfFinancialInstitutionPage, contactName).get

  val addresses: Seq[AddressLookup] = Seq(
    AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
    AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
  )

  val addressOptions: Seq[RadioItem] = Seq(
    RadioItem(content = Text("1 Address line 1, Town, ZZ1 1ZZ"), value = Some("1 Address line 1, Town, ZZ1 1ZZ")),
    RadioItem(content = Text("2 Address line 1, Town, ZZ1 1ZZ"), value = Some("2 Address line 1, Town, ZZ1 1ZZ"))
  )

  val userAnswers = ua
    .set(AddressLookupPage, addresses)
    .success
    .value

  "InstitutionSelectAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, institutionSelectAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InstitutionSelectAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, addressOptions, "fiName", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to manual UK address page if there are no address matches" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, institutionSelectAddressRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.InstitutionUkAddressController.onPageLoad(NormalMode).url
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, institutionSelectAddressRoute).withFormUrlEncodedBody(("value", "1 Address line 1, Town, ZZ1 1ZZ"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, institutionSelectAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[InstitutionSelectAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, addressOptions, "fiName", NormalMode)(request, messages(application)).toString
      }
    }
  }

}
