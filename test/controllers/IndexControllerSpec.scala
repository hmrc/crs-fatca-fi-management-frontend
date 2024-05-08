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
import models.subscription.request.{ContactInformation, IndividualDetails}
import models.subscription.response.UserSubscription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import views.html.IndexView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "Index Controller" - {

    "must return OK and the correct view for a GET" in {

      val individualSubscription = UserSubscription("", None, true, ContactInformation(IndividualDetails("firstName", "lastName"), "", None), None)

      val mockSubscriptionService = mock[SubscriptionService]
      val mockSessionRepository   = mock[SessionRepository]
      val mockAppConfig           = mock[FrontendAppConfig]

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSubscriptionService.getSubscription("FATCAID")).thenReturn(Future.successful(individualSubscription))
      when(mockAppConfig.registerIndividualDetailsUrl) thenReturn "url"
      when(mockAppConfig.registerOrganisationDetailsUrl) thenReturn "url"

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

        contentAsString(result) mustEqual view(true, "businessName", "", NormalMode)(request, messages(application)).toString
      }
    }
  }
  }

}
