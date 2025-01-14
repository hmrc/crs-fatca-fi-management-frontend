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

import models.{CompanyRegistrationNumber, UniqueTaxpayerReference}
import pages.{CompanyRegistrationNumberPage, TrustURNPage}
import pages.addFinancialInstitution.behaviours.PageBehaviours

class WhichIdentificationNumbersPageSpec extends PageBehaviours {

  "cleanUpUnselectedTINPages" - {
    "must remove pages for unselected TINs - CRN & TRN not selected" in {
      val userAnswers = emptyUserAnswers
        .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("222333444"))
        .withPage(CompanyRegistrationNumberPage, CompanyRegistrationNumber("test"))
        .withPage(TrustURNPage, "someTRN")

      val selectedTINs = Set("UTR")
      val result       = WhichIdentificationNumbersPage.cleanUpUnselectedTINPages(selectedTINs, userAnswers).success.value

      result.get(WhatIsUniqueTaxpayerReferencePage).get mustEqual UniqueTaxpayerReference("222333444")
      result.get(CompanyRegistrationNumberPage) mustBe empty
      result.get(TrustURNPage) mustBe empty
    }

    "must remove pages for unselected TINs - UTR & TRN not selected" in {
      val userAnswers = emptyUserAnswers
        .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("222333444"))
        .withPage(CompanyRegistrationNumberPage, CompanyRegistrationNumber("test"))
        .withPage(TrustURNPage, "someTRN")

      val selectedTINs = Set("CRN")
      val result       = WhichIdentificationNumbersPage.cleanUpUnselectedTINPages(selectedTINs, userAnswers).success.value

      result.get(WhatIsUniqueTaxpayerReferencePage) mustBe empty
      result.get(CompanyRegistrationNumberPage).get mustEqual CompanyRegistrationNumber("test")
      result.get(TrustURNPage) mustBe empty
    }

    "must remove pages for unselected TINs - TRN not selected" in {
      val userAnswers = emptyUserAnswers
        .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("222333444"))
        .withPage(CompanyRegistrationNumberPage, CompanyRegistrationNumber("test"))
        .withPage(TrustURNPage, "someTRN")

      val selectedTINs = Set("UTR", "CRN")
      val result       = WhichIdentificationNumbersPage.cleanUpUnselectedTINPages(selectedTINs, userAnswers).success.value

      result.get(WhatIsUniqueTaxpayerReferencePage).get mustEqual UniqueTaxpayerReference("222333444")
      result.get(CompanyRegistrationNumberPage).get mustEqual CompanyRegistrationNumber("test")
      result.get(TrustURNPage) mustBe empty
    }

    "must remove pages for unselected TINs - UTR & CRN not selected" in {
      val userAnswers = emptyUserAnswers
        .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("222333444"))
        .withPage(CompanyRegistrationNumberPage, CompanyRegistrationNumber("test"))
        .withPage(TrustURNPage, "someTRN")

      val selectedTINs = Set("TRN")
      val result       = WhichIdentificationNumbersPage.cleanUpUnselectedTINPages(selectedTINs, userAnswers).success.value

      result.get(WhatIsUniqueTaxpayerReferencePage) mustBe empty
      result.get(CompanyRegistrationNumberPage) mustBe empty
      result.get(TrustURNPage).get mustEqual "someTRN"
    }
  }

}
