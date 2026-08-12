/*
 * Copyright 2026 HM Revenue & Customs
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

import models.FinancialInstitutions.TINType
import models.{CompanyRegistrationNumber, TrustUniqueReferenceNumber, UniqueTaxpayerReference}
import pages.addFinancialInstitution.behaviours.PageBehaviours
import pages.{CompanyRegistrationNumberPage, TrustURNPage}

class HaveIdentificationNumbersPageSpec extends PageBehaviours {

  "HaveIdentificationNumbersPage" - {

    beRetrievable[Boolean](HaveIdentificationNumbersPage)

    beSettable[Boolean](HaveIdentificationNumbersPage)

    beRemovable[Boolean](HaveIdentificationNumbersPage)

    "cleanup" - {
      val selectedTINs: Set[TINType] = Set(TINType.UTR, TINType.CRN, TINType.TURN)

      val userAnswers = emptyUserAnswers
        .withPage(WhichIdentificationNumbersPage, selectedTINs)
        .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("test"))
        .withPage(CompanyRegistrationNumberPage, CompanyRegistrationNumber("test"))
        .withPage(TrustURNPage, TrustUniqueReferenceNumber("test"))

      "must remove dependent pages when false" in {

        val result = HaveIdentificationNumbersPage.cleanup(Some(false), userAnswers).success.value

        result.get(WhichIdentificationNumbersPage) mustBe empty
        result.get(WhatIsUniqueTaxpayerReferencePage) mustBe empty
        result.get(CompanyRegistrationNumberPage) mustBe empty
        result.get(TrustURNPage) mustBe empty
      }

      "must not remove dependent pages when true or None" - {

        Seq(Some(true), None).foreach {
          value =>
            s"when value is $value" in {

              val result = HaveIdentificationNumbersPage.cleanup(value, userAnswers).success.value

              result.get(WhichIdentificationNumbersPage).get mustEqual Set(TINType.UTR, TINType.CRN, TINType.TURN)
              result.get(WhatIsUniqueTaxpayerReferencePage).get mustEqual UniqueTaxpayerReference("test")
              result.get(CompanyRegistrationNumberPage).get mustEqual CompanyRegistrationNumber("test")
              result.get(TrustURNPage).get mustEqual TrustUniqueReferenceNumber("test")
            }
        }
      }
    }
  }

}
