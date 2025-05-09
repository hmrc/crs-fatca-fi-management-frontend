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
import forms.addFinancialInstitution.CompanyRegistrationNumberFormProvider
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.addFinancialInstitution.WhatIsCompanyRegistrationNumberView

class WhatIsCompanyRegistrationNumberControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new CompanyRegistrationNumberFormProvider()
  val form         = formProvider()

  lazy val whatIsCompanyRegistrationNumberRoute = routes.WhatIsCompanyRegistrationNumberController.onPageLoad(NormalMode).url

  "WhatIsCompanyRegistrationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(NameOfFinancialInstitutionPage, "Financial Institution")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsCompanyRegistrationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsCompanyRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(NameOfFinancialInstitutionPage, "Financial Institution")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsCompanyRegistrationNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatIsCompanyRegistrationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName)(request, messages(application)).toString
      }
    }

  }

}
