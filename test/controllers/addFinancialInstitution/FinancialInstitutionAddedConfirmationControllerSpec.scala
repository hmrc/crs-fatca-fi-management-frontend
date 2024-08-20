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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{FinancialInstitutionAddedConfirmationView, ThereIsAProblemView}

class FinancialInstitutionAddedConfirmationControllerSpec extends SpecBase {

  "FinancialInstitutionAddedConfirmationController" - {

    "must return OK and the correct view for a GET" in {
      forAll {
        fiName: String =>
          val fiId = "ABC00000122" // TODO: Replace placeholder FI ID with actual implementation when determined

          val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, fiName)
          val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.FinancialInstitutionAddedConfirmationController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[FinancialInstitutionAddedConfirmationView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(fiName, fiId)(request, messages(application)).toString
          }
      }
    }

    "must return OK and the there-is-a-problem view for a GET when name of FI is missing from user answers" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

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
