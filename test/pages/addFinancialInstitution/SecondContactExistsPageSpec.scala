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

class SecondContactExistsPageSpec extends PageBehaviours {

  "SecondContactExistsPage" - {
    beRetrievable[Boolean](SecondContactExistsPage)

    beSettable[Boolean](SecondContactExistsPage)

    beRemovable[Boolean](SecondContactExistsPage)
  }

  "cleanup" - {
    "must remove second contact pages when answer is false" in {
      forAll {
        (name: String, email: String, contactHavePhone: Boolean, contactPhoneNumber: String) =>
          val userAnswers = emptyUserAnswers
            .withPage(SecondContactNamePage, name)
            .withPage(SecondContactEmailPage, email)
            .withPage(SecondContactCanWePhonePage, contactHavePhone)
            .withPage(SecondContactPhoneNumberPage, contactPhoneNumber)

          val result = SecondContactExistsPage.cleanup(Some(false), userAnswers).success.value

          result.get(SecondContactNamePage) mustBe empty
          result.get(SecondContactEmailPage) mustBe empty
          result.get(SecondContactCanWePhonePage) mustBe empty
          result.get(SecondContactPhoneNumberPage) mustBe empty
      }
    }

    "must not remove second contact pages when answer is true" in {
      forAll {
        (name: String, email: String, contactHavePhone: Boolean, contactPhoneNumber: String) =>
          val userAnswers = emptyUserAnswers
            .withPage(SecondContactNamePage, name)
            .withPage(SecondContactEmailPage, email)
            .withPage(SecondContactCanWePhonePage, contactHavePhone)
            .withPage(SecondContactPhoneNumberPage, contactPhoneNumber)

          val result = SecondContactExistsPage.cleanup(Some(true), userAnswers).success.value

          result.get(SecondContactNamePage) must not be empty
          result.get(SecondContactEmailPage) must not be empty
          result.get(SecondContactCanWePhonePage) must not be empty
          result.get(SecondContactPhoneNumberPage) must not be empty
      }
    }
  }

}
