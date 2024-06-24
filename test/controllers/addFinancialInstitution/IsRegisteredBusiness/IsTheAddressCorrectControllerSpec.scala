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
import controllers.actions.{CtUtrRetrievalAction, FakeCtUtrRetrievalAction}
import controllers.routes
import forms.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, IsTheAddressCorrectPage}
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.RegistrationWithUtrService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectView

import scala.concurrent.{ExecutionContext, Future}

class IsTheAddressCorrectControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new IsTheAddressCorrectFormProvider()
  val form         = formProvider()
  val address      = testAddressResponse

  val userAnswersWithName = UserAnswers(userAnswersId)
    .withPage(NameOfFinancialInstitutionPage, fiName)

  lazy val isTheAddressCorrectRoute: String =
    controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(NormalMode).url

  val mockRegService: RegistrationWithUtrService     = mock[RegistrationWithUtrService]
  val mockCtUtrRetrievalAction: CtUtrRetrievalAction = mock[CtUtrRetrievalAction]
  val mockSessionRepository: SessionRepository       = mock[SessionRepository]

  when(mockRegService.fetchAddress(any())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(Future.successful(testAddressResponse))

  when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction())
  when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

  "IsTheAddressCorrect Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithName))
        .overrides(bind[CtUtrRetrievalAction].toInstance(mockCtUtrRetrievalAction))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[RegistrationWithUtrService].toInstance(mockRegService))
        .build()

      running(application) {
        val request = FakeRequest(GET, isTheAddressCorrectRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsTheAddressCorrectView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName, address)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersWithName
        .withPage(IsTheAddressCorrectPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CtUtrRetrievalAction].toInstance(mockCtUtrRetrievalAction))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[RegistrationWithUtrService].toInstance(mockRegService))
        .build()

      running(application) {
        val request = FakeRequest(GET, isTheAddressCorrectRoute)

        val view = application.injector.instanceOf[IsTheAddressCorrectView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, fiName, address)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = userAnswersWithName
        .withPage(FetchedRegisteredAddressPage, testAddressResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, isTheAddressCorrectRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = userAnswersWithName
        .withPage(FetchedRegisteredAddressPage, testAddressResponse)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, isTheAddressCorrectRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsTheAddressCorrectView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName, address)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, isTheAddressCorrectRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isTheAddressCorrectRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
