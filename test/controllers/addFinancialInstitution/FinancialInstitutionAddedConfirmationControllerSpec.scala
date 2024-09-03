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

package controllers.addFinancialInstitution

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.{FinancialInstitutionAddedConfirmationView, ThereIsAProblemView}

import scala.concurrent.Future

class FinancialInstitutionAddedConfirmationControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  private val fiId = "ABC00000122" // TODO: Replace placeholder FI ID with actual implementation when determined

  "FinancialInstitutionAddedConfirmationController" - {

    "must return OK and the correct view for a GET" in {
      forAll {
        fiName: String =>
          val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
          val application = applicationBuilder(userAnswers = Option(userAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          when(mockSessionRepository.set(userAnswers.copy(data = Json.obj())))
            .thenReturn(Future.successful(true))

          running(application) {
            val request = FakeRequest(GET, routes.FinancialInstitutionAddedConfirmationController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[FinancialInstitutionAddedConfirmationView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(fiName, fiId)(request, messages(application)).toString
          }
      }
    }

    "must return OK and the there-is-a-problem view for a GET when unable to empty user answers data" in {
      forAll {
        fiName: String =>
          val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
          val application = applicationBuilder(userAnswers = Option(userAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(false))

          running(application) {
            val request = FakeRequest(GET, routes.FinancialInstitutionAddedConfirmationController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[ThereIsAProblemView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view()(request, messages(application)).toString
          }
      }
    }
  }

}
