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

package pages.addFinancialInstitution

import pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage
import pages.addFinancialInstitution.behaviours.PageBehaviours

class WhereIsFIBasedPageSpec extends PageBehaviours {

  "WhereIsFIBasedPage" - {
    beRetrievable[Boolean](WhereIsFIBasedPage)

    beSettable[Boolean](WhereIsFIBasedPage)

    beRemovable[Boolean](WhereIsFIBasedPage)
  }

  "cleanup" - {

    "when false" in {
      val ua1 = userAnswersForAddFI
        .withPage(AddressLookupPage, Seq(testAddressLookup))
        .withPage(SelectAddressPage, "Some address")

      val ua2 = userAnswersForAddFI
        .withPage(FetchedRegisteredAddressPage, testAddressResponse)

      val ua3 = userAnswersForAddFI
        .withPage(UkAddressPage, testAddress)

      Seq(ua1, ua2, ua3).foreach {
        ua =>
          val result = WhereIsFIBasedPage.cleanup(Some(false), ua)
          result.get.data.value must not contain key(SelectedAddressLookupPage.toString)
          result.get.data.value must not contain key(PostcodePage.toString)
          result.get.data.value must not contain key(IsThisAddressPage.toString)
          result.get.data.value must not contain key(AddressLookupPage.toString)
          result.get.data.value must not contain key(SelectAddressPage.toString)
          result.get.data.value must not contain key(UkAddressPage.toString)
          result.get.data.value must not contain key(FetchedRegisteredAddressPage.toString)
      }
    }

    "when true" in {
      val result = WhereIsFIBasedPage.cleanup(Some(true), userAnswersForAddFI)
      result.get.data.value must not contain key(NonUkAddressPage.toString)

    }
  }

}
