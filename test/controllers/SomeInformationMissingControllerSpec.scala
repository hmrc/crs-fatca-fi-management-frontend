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
import models.NormalMode
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SomeInformationMissingView

class SomeInformationMissingControllerSpec extends SpecBase {

  "SomeInformationMissing Controller" - {

    "must return OK and the correct view for a GET when fi is not user" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SomeInformationMissingController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SomeInformationMissingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(controllers.addFinancialInstitution.routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when fi is user" in {

      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .withPage(ReportForRegisteredBusinessPage, true)
        )
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.SomeInformationMissingController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SomeInformationMissingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode).url
        )(request, messages(application)).toString
      }
    }
  }

}
