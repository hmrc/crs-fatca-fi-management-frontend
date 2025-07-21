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
import forms.addFinancialInstitution.YourFinancialInstitutionsFormProvider
import models.FinancialInstitutions.{AddressDetails, FIDetail}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import utils.ViewHelper
import viewmodels.govuk.all.SummaryListViewModelNoMargin
import viewmodels.yourFinancialInstitutions.YourFinancialInstitutionsViewModel
import views.html.YourFinancialInstitutionsView

class YourFinancialInstitutionsViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  private val view                                = app.injector.instanceOf[YourFinancialInstitutionsView]
  private val formProvider                        = new YourFinancialInstitutionsFormProvider()
  private val form                                = formProvider()
  private val messagesControllerComponentsForView = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "YourFinancialInstitutionsView" - {
    "should render page components" in {

      val summaryListViewModel = SummaryListViewModelNoMargin(
        YourFinancialInstitutionsViewModel.getYourFinancialInstitutionsRows(
          Seq(
            FIDetail(
              FIID = "12345",
              FIName = "Test Financial Institution",
              SubscriptionID = "sub12345",
              TINDetails = Seq.empty,
              GIIN = None,
              IsFIUser = false,
              AddressDetails = AddressDetails(
                AddressLine1 = "123 Test Street",
                AddressLine2 = Some("Test City"),
                AddressLine3 = None,
                AddressLine4 = None,
                CountryCode = Some("GB"),
                PostalCode = Some("AB12 3CD")
              ),
              PrimaryContactDetails = None,
              SecondaryContactDetails = None
            )
          )
        )
      )

      val renderedHtml = view(form, summaryListViewModel)

      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Manage your financial institutions")
      getPageHeading(doc) mustEqual "You have added 1 financial institution"
      doc.body.select("dt").text() must include("Test Financial Institution")
    }
  }

}
