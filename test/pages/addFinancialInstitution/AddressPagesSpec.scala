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
      "must remove other address answers from UserAnswers" in {
        val res1: UserAnswers = emptyUserAnswers
          .set(FetchedRegisteredAddressPage, testAddressResponse)
          .get
          .set(UkAddressPage, testAddress)
          .get

        val res2: UserAnswers = emptyUserAnswers
          .set(NonUkAddressPage, testAddress)
          .get
          .set(UkAddressPage, testAddress)
          .get

        val res3: UserAnswers = emptyUserAnswers
          .set(SelectedAddressLookupPage, testAddressLookup)
          .get
          .set(UkAddressPage, testAddress)
          .get

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
          .set(FetchedRegisteredAddressPage, testAddressResponse)
          .get
          .set(NonUkAddressPage, testAddress)
          .get

        val res2: UserAnswers = emptyUserAnswers
          .set(UkAddressPage, testAddress)
          .get
          .set(NonUkAddressPage, testAddress)
          .get

        val res3: UserAnswers = emptyUserAnswers
          .set(SelectedAddressLookupPage, testAddressLookup)
          .get
          .set(NonUkAddressPage, testAddress)
          .get

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
          .set(FetchedRegisteredAddressPage, testAddressResponse)
          .get
          .set(SelectedAddressLookupPage, testAddressLookup)
          .get

        val res2: UserAnswers = emptyUserAnswers
          .set(UkAddressPage, testAddress)
          .get
          .set(SelectedAddressLookupPage, testAddressLookup)
          .get

        val res3: UserAnswers = emptyUserAnswers
          .set(NonUkAddressPage, testAddress)
          .get
          .set(SelectedAddressLookupPage, testAddressLookup)
          .get

        res1.get(SelectedAddressLookupPage) mustBe defined
        res1.data.fields.size mustBe 1

        res2.get(SelectedAddressLookupPage) mustBe defined
        res2.data.fields.size mustBe 1

        res3.get(SelectedAddressLookupPage) mustBe defined
        res3.data.fields.size mustBe 1

      }

    }
    "FetchedRegisteredAddressPage" - {
      "must remove other address answers from UserAnswers" in {
        val res1: UserAnswers = emptyUserAnswers
          .set(SelectedAddressLookupPage, testAddressLookup)
          .get
          .set(FetchedRegisteredAddressPage, testAddressResponse)
          .get

        val res2: UserAnswers = emptyUserAnswers
          .set(UkAddressPage, testAddress)
          .get
          .set(FetchedRegisteredAddressPage, testAddressResponse)
          .get

        val res3: UserAnswers = emptyUserAnswers
          .set(NonUkAddressPage, testAddress)
          .get
          .set(FetchedRegisteredAddressPage, testAddressResponse)
          .get

        res1.get(FetchedRegisteredAddressPage) mustBe defined
        res1.data.fields.size mustBe 1

        res2.get(FetchedRegisteredAddressPage) mustBe defined
        res2.data.fields.size mustBe 1

        res3.get(FetchedRegisteredAddressPage) mustBe defined
        res3.data.fields.size mustBe 1

      }
    }

  }

}
