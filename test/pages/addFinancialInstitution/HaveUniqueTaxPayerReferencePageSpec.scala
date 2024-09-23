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

import models.UniqueTaxpayerReference
import pages.addFinancialInstitution.behaviours.PageBehaviours

class HaveUniqueTaxPayerReferencePageSpec extends PageBehaviours {

  "HaveUniqueTaxpayerReferencePage" - {
    beRetrievable[Boolean](HaveUniqueTaxpayerReferencePage)

    beSettable[Boolean](HaveUniqueTaxpayerReferencePage)

    beRemovable[Boolean](HaveUniqueTaxpayerReferencePage)
  }

  "cleanup" - {
    "must remove WhatIsUniqueTaxpayerReferencePage when answer is false" in {
      forAll(validUtr -> "UTR") {
        value: String =>
          val userAnswers = emptyUserAnswers.withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference(value))

          val result = HaveUniqueTaxpayerReferencePage.cleanup(Some(false), userAnswers).success.value

          result.get(WhatIsUniqueTaxpayerReferencePage) mustBe empty
      }
    }

    "must not remove WhatIsUniqueTaxpayerReferencePage when answer is true" in {
      forAll(validUtr -> "UTR") {
        value: String =>
          val userAnswers = emptyUserAnswers.withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference(value))

          val result = HaveUniqueTaxpayerReferencePage.cleanup(Some(true), userAnswers).success.value

          result.get(WhatIsUniqueTaxpayerReferencePage) must not be empty
      }
    }
  }

}
