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

import base.SpecBase
import models.UserAnswers
import pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage

class AddressPagesSpec extends SpecBase {

  "cleanup for" - {
    "UkAddressPage" - {
      "must remove other address answers from UserAnswers" - {
        val res1: UserAnswers = emptyUserAnswers
          .withPage(FetchedRegisteredAddressPage, testAddressResponse)
          .withPage(UkAddressPage, testAddress)

        val res2: UserAnswers = emptyUserAnswers
          .withPage(NonUkAddressPage, testAddress)
          .withPage(UkAddressPage, testAddress)

        val res3: UserAnswers = emptyUserAnswers
          .withPage(SelectedAddressLookupPage, testAddressLookup)
          .withPage(UkAddressPage, testAddress)

        res1.get(UkAddressPage) mustBe defined
        res1.data.fields.size mustBe 1

        res2.get(UkAddressPage) mustBe defined
        res2.data.fields.size mustBe 1

        res3.get(UkAddressPage) mustBe defined
        res3.data.fields.size mustBe 1

      }
    }
    "NonUkAddressPage" - {
      "must remove other address answers from UserAnswers" in {
        val res1: UserAnswers = emptyUserAnswers
          .withPage(FetchedRegisteredAddressPage, testAddressResponse)
          .withPage(NonUkAddressPage, testAddress)

        val res2: UserAnswers = emptyUserAnswers
          .withPage(UkAddressPage, testAddress)
          .withPage(NonUkAddressPage, testAddress)

        val res3: UserAnswers = emptyUserAnswers
          .withPage(SelectedAddressLookupPage, testAddressLookup)
          .withPage(NonUkAddressPage, testAddress)

        res1.get(NonUkAddressPage) mustBe defined
        res1.data.fields.size mustBe 1

        res2.get(NonUkAddressPage) mustBe defined
        res2.data.fields.size mustBe 1

        res3.get(NonUkAddressPage) mustBe defined
        res3.data.fields.size mustBe 1

      }
    }
    "SelectedAddressLookupPage" - {
      "must remove other address answers from UserAnswers" in {
        val res1: UserAnswers = emptyUserAnswers
          .withPage(FetchedRegisteredAddressPage, testAddressResponse)
          .withPage(SelectedAddressLookupPage, testAddressLookup)

        val res2: UserAnswers = emptyUserAnswers
          .withPage(UkAddressPage, testAddress)
          .withPage(SelectedAddressLookupPage, testAddressLookup)

        val res3: UserAnswers = emptyUserAnswers
          .withPage(NonUkAddressPage, testAddress)
          .withPage(SelectedAddressLookupPage, testAddressLookup)

        res1.get(SelectedAddressLookupPage) mustBe defined
        res1.data.fields.size mustBe 1

        res2.get(SelectedAddressLookupPage) mustBe defined
        res2.data.fields.size mustBe 1

        res3.get(SelectedAddressLookupPage) mustBe defined
        res3.data.fields.size mustBe 1

      }

    }

  }

}
