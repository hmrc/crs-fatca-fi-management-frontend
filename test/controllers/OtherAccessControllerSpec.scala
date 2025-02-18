/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.OtherAccessFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.OtherAccessView

import scala.concurrent.{ExecutionContext, Future}

class OtherAccessControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new OtherAccessFormProvider()
  val form: Form[Boolean] = formProvider()

  val fiIsUser = true

  lazy val otherAccessRoute: String                                  = routes.OtherAccessController.onPageLoad(testFiDetail.FIID).url
  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]

  "OtherAccess Controller" - {

    when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(testFiDetails))

    when(mockFinancialInstitutionsService.getInstitutionById(Seq(any()), any())).thenReturn(Some(testFiDetail))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, otherAccessRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OtherAccessView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, fiIsUser, testFiDetail.FIID, testFiDetail.FIName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, otherAccessRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RemoveAreYouSureController.onPageLoad(testFiid).url
      }
    }
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, otherAccessRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[OtherAccessView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, fiIsUser, testFiDetail.FIID, testFiDetail.FIName)(request, messages(application)).toString
      }
    }

  }

}
