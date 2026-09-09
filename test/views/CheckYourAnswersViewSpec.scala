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

package views

import base.SpecBase
import forms.UserAccessFormProvider
import models.CheckAnswers
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import viewmodels.checkAnswers.CheckYourAnswersViewModel.getFinancialInstitutionSummaries
import viewmodels.common.{getFirstContactSummaries, getSecondContactSummaries}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.addFinancialInstitution.CheckYourAnswersView

class CheckYourAnswersViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view1: CheckYourAnswersView                                       = app.injector.instanceOf[CheckYourAnswersView]
  val form                                                              = new UserAccessFormProvider()
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "CheckYourAnswersView" - {
    "should render page components" in {

      val financialInstitutionList            = SummaryListViewModel(getFinancialInstitutionSummaries(userAnswersForAddFI))
      val firstContactList                    = SummaryListViewModel(getFirstContactSummaries(userAnswersForAddFI, CheckAnswers))
      val secondContactList                   = SummaryListViewModel(getSecondContactSummaries(userAnswersForAddFI, CheckAnswers))
      val renderedHtml: HtmlFormat.Appendable = view1("testFIName", financialInstitutionList, firstContactList, secondContactList)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Check the details for the financial institution")
      getPageHeading(doc) mustEqual "Check the details for testFIName"
      getSubheadingText(doc, 0) must include("First contact")
      getSubheadingText(doc, 1) must include("Second contact")
      elementText(doc, "#submit") mustEqual "Confirm and add"
    }
  }

}
