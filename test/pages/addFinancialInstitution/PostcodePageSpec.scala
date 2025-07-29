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

import pages.addFinancialInstitution.behaviours.PageBehaviours

class PostcodePageSpec extends PageBehaviours {

  "cleanup" - {

    "when clean up is triggered when IsThisAddressPage is available" in {
      val updatedUserAnswer = userAnswersForAddFI.withPage(IsThisAddressPage, true)
      val result            = PostcodePage.cleanup(Some("ZZ11ZZ"), updatedUserAnswer)

      result.get.data.value must not contain key(IsThisAddressPage.toString)
    }

    "when clean up is triggered when IsThisAddressPage is not available" in {
      val result = PostcodePage.cleanup(Some("ZZ11ZZ"), userAnswersForAddFI)

      result.get.data.value must not contain key(IsThisAddressPage.toString)
    }
  }

}
