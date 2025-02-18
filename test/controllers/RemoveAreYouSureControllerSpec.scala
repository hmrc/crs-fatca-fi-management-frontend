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
import forms.RemoveAreYouSureFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{OtherAccessPage, RemoveInstitutionDetail}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.RemoveAreYouSureView

import scala.concurrent.{ExecutionContext, Future}

class RemoveAreYouSureControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider                                                   = new RemoveAreYouSureFormProvider()
  val form: Form[Boolean]                                            = formProvider()
  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]
  val mockSessionRepository: SessionRepository                       = mock[SessionRepository]
  lazy val removeAreYouSureRoute: String                             = routes.RemoveAreYouSureController.onPageLoad(testFiDetail.FIID).url

  "RemoveAreYouSure Controller" - {
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
    when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(testFiDetails))
    when(mockFinancialInstitutionsService.getInstitutionById(Seq(any()), any())).thenReturn(Some(testFiDetail))

    "must return OK and the correct view for a GET after YES on previous page" in {

      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers.withPage(OtherAccessPage, true)))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, removeAreYouSureRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAreYouSureView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, testFiDetail.FIID, testFiDetail.FIName, warningUnderstood = true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET after NO on previous page" in {

      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers.withPage(OtherAccessPage, false)))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, removeAreYouSureRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAreYouSureView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, testFiDetail.FIID, testFiDetail.FIName, warningUnderstood = false)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId).set(RemoveInstitutionDetail, testFiDetail).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockFinancialInstitutionsService.removeFinancialInstitution(any())(any(), any())) thenReturn Future.successful(HttpResponse(OK, ""))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAreYouSureRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId).set(RemoveInstitutionDetail, testFiDetail).success.value

      val application = applicationBuilder(userAnswers = Option(userAnswers))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAreYouSureRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveAreYouSureView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, testFiDetail.FIID, testFiDetail.FIName, warningUnderstood = true)(request,
                                                                                                                            messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAreYouSureRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
