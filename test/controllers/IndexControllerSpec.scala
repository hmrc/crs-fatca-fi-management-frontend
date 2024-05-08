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
import connectors.FileDetailsConnector
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import views.html.IndexView

class IndexControllerSpec extends SpecBase {

  "Index Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockSubscriptionService = mock[SubscriptionService]
      val mockSessionRepository   = mock[SessionRepository]

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository)
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
