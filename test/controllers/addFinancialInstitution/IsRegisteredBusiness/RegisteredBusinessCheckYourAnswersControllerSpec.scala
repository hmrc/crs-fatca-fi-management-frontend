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
import controllers.routes
import pages.InformationSentPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.addFinancialInstitution.IsRegisteredBusiness.RegisteredBusinessCheckYourAnswersView

class RegisteredBusinessCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val list: SummaryList = SummaryListViewModel(Seq.empty)

  "RegisteredBusinessCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegisteredBusinessCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiName, list)(request, messages(application)).toString
      }
    }

    "must redirect to information-sent page for a GET when the information-sent flag is true" in {
      val userAnswers = emptyUserAnswers.withPage(InformationSentPage, true)
      val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad.url
      }
    }

    "confirmAndAdd" - {
      "must redirect to self (until the PUT endpoint exists)" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController
            .onPageLoad()
            .url

        }
      }

      "must redirect to information-sent page for a GET when the information-sent flag is true" in {
        val userAnswers = emptyUserAnswers.withPage(InformationSentPage, true)
        val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad.url
        }
      }
    }

  }

}
