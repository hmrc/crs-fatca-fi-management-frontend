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

package pages.addFinancialInstitution.IsRegisteredBusiness

import models.AddressResponse
import pages.addFinancialInstitution.behaviours.PageBehaviours
import pages.addFinancialInstitution.{AddressLookupPage, SelectedAddressLookupPage, UkAddressPage}

class FetchedRegisteredAddressPageSpec extends PageBehaviours {

  "IsTheAddressCorrectPage" - {
    beRetrievable[AddressResponse](FetchedRegisteredAddressPage)

    beSettable[AddressResponse](FetchedRegisteredAddressPage)

    beRemovable[AddressResponse](FetchedRegisteredAddressPage)
  }

  "cleanup" - {

    val pagesToClear = Table(
      ("page", "setupUserAnswers"),
      (SelectedAddressLookupPage, emptyUserAnswers.withPage(SelectedAddressLookupPage, testAddressLookup)),
      (UkAddressPage, emptyUserAnswers.withPage(UkAddressPage, testAddress)),
      (AddressLookupPage, emptyUserAnswers.withPage(AddressLookupPage, Seq(testAddressLookup)))
    )

    forAll(pagesToClear) {
      (page, userAnswers) =>
        s"clears ${page.getClass.getSimpleName.replace("$", "")} when set" in {
          val result = FetchedRegisteredAddressPage.cleanup(Some(testAddressResponse), userAnswers)

          result.get.data.value must not contain key(page.toString)
        }
    }
  }

}
