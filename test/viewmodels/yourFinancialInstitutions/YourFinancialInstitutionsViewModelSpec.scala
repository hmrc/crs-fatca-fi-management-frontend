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

package viewmodels.yourFinancialInstitutions

import base.SpecBase
import models.FinancialInstitutions.FIDetail
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class YourFinancialInstitutionsViewModelSpec extends SpecBase {

  val viewModelSut             = YourFinancialInstitutionsViewModel
  val privateOrderInstitutions = PrivateMethod[Seq[FIDetail]](Symbol("orderInstitutions"))
  val privateGetValueContent   = PrivateMethod[HtmlContent](Symbol("getValueContent"))

  "privateOrderInstitutions" - {
    "must order the institutions alphabetically" in {
      val listOfInstitutions: Seq[FIDetail] = Seq(
        testFiDetail.copy(FIName = "Theta", IsFIUser = false),
        testFiDetail.copy(FIName = "Alpha", IsFIUser = false),
        testFiDetail.copy(FIName = "Beta", IsFIUser = false)
      )
      val expectedOrderedDetails = Seq(
        testFiDetail.copy(FIName = "Alpha", IsFIUser = false),
        testFiDetail.copy(FIName = "Beta", IsFIUser = false),
        testFiDetail.copy(FIName = "Theta", IsFIUser = false)
      )
      val result = viewModelSut.invokePrivate(privateOrderInstitutions(listOfInstitutions))
      result mustBe expectedOrderedDetails
    }
    "must put IsFIUser = true at the top of the list" in {
      val listOfInstitutions: Seq[FIDetail] = Seq(
        testFiDetail.copy(FIName = "Alpha", IsFIUser = false),
        testFiDetail.copy(FIName = "Beta", IsFIUser = false),
        testFiDetail.copy(FIName = "Theta", IsFIUser = true)
      )
      val expectedOrderedDetails = Seq(
        testFiDetail.copy(FIName = "Theta", IsFIUser = true),
        testFiDetail.copy(FIName = "Alpha", IsFIUser = false),
        testFiDetail.copy(FIName = "Beta", IsFIUser = false)
      )
      val result = viewModelSut.invokePrivate(privateOrderInstitutions(listOfInstitutions))
      result mustBe expectedOrderedDetails
    }
  }

  "getValueContent" - {
    "must only show the fi name when fi is not the registered business" in {
      val result = viewModelSut.invokePrivate(privateGetValueContent("some financial institution", false))
      result mustBe HtmlContent("""<span class="govuk-!-margin-right-2" style="max-width: 180px">some financial institution</span>""")
    }
    "must add a tag to the fi name when fi is the registered business" in {
      val result = viewModelSut.invokePrivate(privateGetValueContent("some financial institution", true))
      result mustBe HtmlContent(
        """<span class="govuk-!-margin-right-2" style="max-width: 180px">some financial institution</span>
          |<strong class="govuk-tag" style="max-width: 180px !important;">Registered business</strong>""".stripMargin
      )
    }
  }

}
