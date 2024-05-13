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
import config.FrontendAppConfig
import models.NormalMode
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails}
import models.subscription.response.UserSubscription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexControllerSpec extends SpecBase {

  val mockSubscriptionService = mock[SubscriptionService]
  val mockSessionRepository   = mock[SessionRepository]
  val mockAppConfig           = mock[FrontendAppConfig]

  when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
  when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

  "Index Controller" - {

    "must return OK and the correct view for a GET with individualSubscription details" in {

      val individualSubscription =
        UserSubscription("FATCAID", None, true, ContactInformation(IndividualDetails("firstName", "lastName"), "test@test.com", None), None)

      when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(individualSubscription))
      when(mockAppConfig.registerIndividualDetailsUrl) thenReturn "/change-contact/individual/details"
      when(mockAppConfig.feedbackUrl(any[RequestHeader]())) thenReturn "test"

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(false, "", "/change-contact/individual/details")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with organisationSubscription details" in {

      val organisationSubscription =
        UserSubscription("FATCAID", None, true, ContactInformation(OrganisationDetails("businessName"), "test@test.com", None), None)

      when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(organisationSubscription))
      when(mockAppConfig.registerOrganisationDetailsUrl) thenReturn "/change-contact/organisation/details"
      when(mockAppConfig.feedbackUrl(any[RequestHeader]())) thenReturn "test"

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(true, "businessName", "/change-contact/organisation/details")(request, messages(application)).toString
      }
    }
  }

}
