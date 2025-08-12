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

package controllers.addFinancialInstitution.IsRegisteredBusiness

import base.SpecBase
import controllers.routes
import forms.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessView

import scala.concurrent.{ExecutionContext, Future}

class ReportForRegisteredBusinessControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider         = new ReportForRegisteredBusinessFormProvider()
  val form                 = formProvider()
  val isChangeFIInProgress = false
  val changeFIDetailHeader = "Is this financial institution the business you registered as?"
  val normalHeader         = "Would you like to add your business as a financial institution?"

  lazy val reportForRegisteredBusinessRoute =
    controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode).url

  lazy val reportForRegisteredBusinessRouteCheckMode =
    controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(CheckMode).url

  val mockFinancialInstitutionsService = mock[FinancialInstitutionsService]

  when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(Future.successful(Seq.empty))

  "ReportForRegisteredBusiness Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, reportForRegisteredBusinessRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportForRegisteredBusinessView]

        status(result) mustEqual OK
        val htmlVal = contentAsString(result)
        htmlVal mustEqual view(form, NormalMode, isChangeFIInProgress)(request, messages(application)).toString
        val heading = Jsoup.parse(htmlVal).select(".govuk-heading-l")
        heading.size() mustEqual 1
        heading.get(0).text() mustEqual normalHeader
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered And ChangeInProgress as False" in {

      val userAnswers = UserAnswers(userAnswersId).set(ReportForRegisteredBusinessPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, reportForRegisteredBusinessRoute)

        val view = application.injector.instanceOf[ReportForRegisteredBusinessView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, isChangeFIInProgress)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered And ChangeInProgress as True" in {

      val userAnswers = UserAnswers(userAnswersId)
        .withPage(ChangeFiDetailsInProgressId, "67867812")
        .withPage(ReportForRegisteredBusinessPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
        )
        .build()

      val changeInProgress = true

      running(application) {
        val request = FakeRequest(GET, reportForRegisteredBusinessRouteCheckMode)

        val view = application.injector.instanceOf[ReportForRegisteredBusinessView]

        val result = route(application, request).value

        status(result) mustEqual OK
        val htmlVal = contentAsString(result)
        htmlVal mustEqual view(form.fill(true), CheckMode, changeInProgress)(request, messages(application)).toString
        val heading = Jsoup.parse(htmlVal).select(".govuk-fieldset__heading")
        heading.size() mustEqual 1
        heading.get(0).text() mustEqual changeFIDetailHeader
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reportForRegisteredBusinessRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    "must redirect to Page Unavailable if user already reports for a registered business" in {

      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(testFiDetails))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, reportForRegisteredBusinessRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, reportForRegisteredBusinessRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReportForRegisteredBusinessView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, isChangeFIInProgress)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, reportForRegisteredBusinessRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, reportForRegisteredBusinessRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
