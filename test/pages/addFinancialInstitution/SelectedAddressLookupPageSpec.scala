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

package pages.addFinancialInstitution

import pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage
import pages.addFinancialInstitution.behaviours.PageBehaviours

class SelectedAddressLookupPageSpec extends PageBehaviours {

  "cleanup" - {

    val pagesToClear = Table(
      ("page", "setupUserAnswers"),
      (UkAddressPage, userAnswersForAddFI.withPage(UkAddressPage, testAddress)),
      (FetchedRegisteredAddressPage, userAnswersForAddFI.withPage(FetchedRegisteredAddressPage, testAddressResponse))
    )

    forAll(pagesToClear) {
      (page, userAnswers) =>
        s"clears ${page.getClass.getSimpleName.replace("$", "")} when set" in {
          val result = SelectedAddressLookupPage.cleanup(Some(testAddressLookup), userAnswers)

          result.get.data.value must not contain key(page.toString)
        }
    }
  }

}
