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
import forms.addFinancialInstitution.IsRegisteredBusiness.IsThisTheBusinessNameFormProvider
import models.subscription.request.{ContactInformation, OrganisationDetails}
import models.subscription.response.UserSubscription
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.addFinancialInstitution.IsRegisteredBusiness.IsThisTheBusinessNamePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.addFinancialInstitution.IsRegisteredBusiness.IsThisTheBusinessNameView

import scala.concurrent.{ExecutionContext, Future}

class IsThisTheBusinessNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider        = new IsThisTheBusinessNameFormProvider()
  val form: Form[Boolean] = formProvider()

  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

  val organisationSubscription: UserSubscription =
    UserSubscription("FATCAID", None, true, ContactInformation(OrganisationDetails("testName"), "test@test.com", None), None)

  when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(Future.successful(organisationSubscription))

  lazy val isThisTheBusinessNameRoute: String =
    controllers.addFinancialInstitution.registeredBusiness.routes.IsThisTheBusinessNameController.onPageLoad(NormalMode).url

  "IsThisTheBusinessName Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, isThisTheBusinessNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisTheBusinessNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "testName")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IsThisTheBusinessNamePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, isThisTheBusinessNameRoute)

        val view = application.injector.instanceOf[IsThisTheBusinessNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, "testName")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheBusinessNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }

}
