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
import models.CheckMode
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.InformationSentPage
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import views.html.addFinancialInstitution.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  implicit val mockMessages: Messages = mock[Messages]

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]
          val list = SummaryListViewModel(Seq.empty)

          val firstContactList = SummaryListViewModel(
            Seq(
              SummaryListRowViewModel(
                key = KeyViewModel(HtmlContent("First contact telephone number")),
                value = ValueViewModel(HtmlContent("Not provided")),
                actions = Seq(
                  ActionItemViewModel(
                    content = HtmlContent(
                      s"""
                         |<span aria-hidden="true">Change</span>
                         |""".stripMargin
                    ),
                    href = controllers.addFinancialInstitution.routes.FirstContactHavePhoneController.onPageLoad(CheckMode).url
                  ).withVisuallyHiddenText("Change first contact telephone number")
                )
              )
            )
          )
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(fiName, list, firstContactList, list)(request, messages(application)).toString
        }
      }

      "must redirect to information-sent page for a GET when the information-sent flag is true" in {
        val userAnswers = emptyUserAnswers.withPage(InformationSentPage, true)
        val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.InformationSentController.onPageLoad.url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    "confirmAndAdd" - {
      "must redirect to self (until the PUT endpoint exists)" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

        }
      }

      "must redirect to information-sent page for a POST when the information-sent flag is true" in {
        val userAnswers = emptyUserAnswers.withPage(InformationSentPage, true)
        val application = applicationBuilder(userAnswers = Option(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.InformationSentController.onPageLoad.url
        }
      }
    }
  }

}
