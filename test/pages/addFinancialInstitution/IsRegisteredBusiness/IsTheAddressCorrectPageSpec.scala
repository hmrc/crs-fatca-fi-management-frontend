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

package pages.addFinancialInstitution.IsRegisteredBusiness

import pages.addFinancialInstitution._
import pages.addFinancialInstitution.behaviours.PageBehaviours

class IsTheAddressCorrectPageSpec extends PageBehaviours {

  "IsTheAddressCorrectPage" - {
    beRetrievable[Boolean](IsTheAddressCorrectPage)

    beSettable[Boolean](IsTheAddressCorrectPage)

    beRemovable[Boolean](IsTheAddressCorrectPage)
  }

  "cleanup" - {

    "when false" in {
      val result = IsTheAddressCorrectPage.cleanup(Some(false), userAnswersForAddFI.withPage(FetchedRegisteredAddressPage, testAddressResponse))

      result.get.data.value must not contain key(FetchedRegisteredAddressPage.toString)
    }

    "when true" - {
      val pagesToClear = Table(
        ("page", "setupUserAnswers"),
        (PostcodePage, emptyUserAnswers.withPage(PostcodePage, "postcode")),
        (SelectedAddressLookupPage, emptyUserAnswers.withPage(SelectedAddressLookupPage, testAddressLookup)),
        (IsThisAddressPage, emptyUserAnswers.withPage(IsThisAddressPage, true)),
        (UkAddressPage, emptyUserAnswers.withPage(UkAddressPage, testAddress)),
        (AddressLookupPage, emptyUserAnswers.withPage(AddressLookupPage, Seq(testAddressLookup)))
      )

      forAll(pagesToClear) {
        (page, userAnswers) =>
          s"clears ${page.getClass.getSimpleName.replace("$", "")} when set" in {
            val result = IsTheAddressCorrectPage.cleanup(Some(true), userAnswers)

            result.get.data.value must not contain key(page.toString)
          }
      }
    }

  }

}
