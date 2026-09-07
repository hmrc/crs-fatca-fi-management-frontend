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

import pages.addFinancialInstitution.behaviours.PageBehaviours

class FirstContactHavePhonePageSpec extends PageBehaviours {

  "FirstContactPhoneNumberPage" - {
    beRetrievable[Boolean](FirstContactHavePhonePage)

    beSettable[Boolean](FirstContactHavePhonePage)

    beRemovable[Boolean](FirstContactHavePhonePage)
  }

  "cleanup" - {
    "must remove FirstContactPhoneNumberPage when answer is false" in {
      forAll {
        value: String =>
          val userAnswers = emptyUserAnswers.withPage(FirstContactPhoneNumberPage, value)

          val result = FirstContactHavePhonePage.cleanup(Some(false), userAnswers).success.value

          result.get(FirstContactPhoneNumberPage) mustBe empty
      }
    }

    "must not remove FirstContactPhoneNumberPage when answer is true" in {
      forAll {
        value: String =>
          val userAnswers = emptyUserAnswers.withPage(FirstContactPhoneNumberPage, value)

          val result = FirstContactHavePhonePage.cleanup(Some(true), userAnswers).success.value

          result.get(FirstContactPhoneNumberPage) must not be empty
      }
    }
  }

}
