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
import generators.Generators
import models.UserAnswers
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.FIRemovedView

import java.time._

class FIRemovedControllerSpec extends SpecBase with Generators {

  "FIRemoved Controller" - {

    val userAnswers: UserAnswers = emptyUserAnswers
    "must return OK and the correct view for a GET when time is midnight" in {

      val midnight         = Instant.parse("2025-02-15T00:00:00Z")
      val stubClock: Clock = Clock.fixed(midnight, ZoneId.systemDefault)

      val ua = userAnswers.set(NameOfFinancialInstitutionPage, fiName).get
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[Clock].toInstance(stubClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FIRemovedController.onPageLoad().url).withFlash("fiName" -> fiDetailName, "fiid" -> testFiid)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FIRemovedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testFiDetail.FIName, testFiDetail.FIID, "15 February 2025", "midnight")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when time is midday" in {

      val midnight         = Instant.parse("2025-02-15T12:00:00Z")
      val stubClock: Clock = Clock.fixed(midnight, ZoneId.systemDefault)

      val ua = userAnswers.set(NameOfFinancialInstitutionPage, fiName).get
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[Clock].toInstance(stubClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FIRemovedController.onPageLoad().url).withFlash("fiName" -> fiDetailName, "fiid" -> testFiid)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FIRemovedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testFiDetail.FIName, testFiDetail.FIID, "15 February 2025", "midday")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when time is other than midday/midnight" in {

      val midnight         = Instant.parse("2025-02-15T12:14:00Z")
      val stubClock: Clock = Clock.fixed(midnight, ZoneId.systemDefault)

      val ua = userAnswers.set(NameOfFinancialInstitutionPage, fiName).get
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[Clock].toInstance(stubClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FIRemovedController.onPageLoad().url).withFlash("fiName" -> fiDetailName, "fiid" -> testFiid)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FIRemovedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testFiDetail.FIName, testFiDetail.FIID, "15 February 2025", "12:14pm")(request, messages(application)).toString
      }
    }

    "must return Journey Recovery Controller with no ID" in {

      val midnight         = Instant.parse("2025-02-15T12:14:00Z")
      val stubClock: Clock = Clock.fixed(midnight, ZoneId.systemDefault)

      val ua = userAnswers.set(NameOfFinancialInstitutionPage, fiName).get
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[Clock].toInstance(stubClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FIRemovedController.onPageLoad().url).withFlash("fiName" -> fiDetailName)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return Journey Recovery Controller with no name" in {

      val midnight         = Instant.parse("2025-02-15T12:14:00Z")
      val stubClock: Clock = Clock.fixed(midnight, ZoneId.systemDefault)

      val ua = userAnswers.set(NameOfFinancialInstitutionPage, fiName).get
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[Clock].toInstance(stubClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FIRemovedController.onPageLoad().url).withFlash("fiid" -> testFiid)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
