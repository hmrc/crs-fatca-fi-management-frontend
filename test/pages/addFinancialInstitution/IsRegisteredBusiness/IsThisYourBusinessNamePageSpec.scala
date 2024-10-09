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

package pages.addFinancialInstitution.IsRegisteredBusiness

import pages.addFinancialInstitution._
import pages.addFinancialInstitution.behaviours.PageBehaviours

class IsThisYourBusinessNamePageSpec extends PageBehaviours {

  "IsThisYourBusinessNamePage" - {
    beRetrievable[Boolean](IsThisYourBusinessNamePage)

    beSettable[Boolean](IsThisYourBusinessNamePage)

    beRemovable[Boolean](IsThisYourBusinessNamePage)
  }

  "cleanup" - {

    "when false" in {
      val result = IsThisYourBusinessNamePage.cleanup(Some(false), userAnswersForAddFI)

      result.get.data.value must contain key NameOfFinancialInstitutionPage.toString
      result.get.data.value must not contain key(HaveUniqueTaxpayerReferencePage.toString)
      result.get.data.value must not contain key(WhatIsUniqueTaxpayerReferencePage.toString)
      result.get.data.value must not contain key(FirstContactNamePage.toString)
      result.get.data.value must not contain key(FirstContactEmailPage.toString)
      result.get.data.value must not contain key(FirstContactHavePhonePage.toString)
      result.get.data.value must not contain key(FirstContactPhoneNumberPage.toString)
      result.get.data.value must not contain key(SecondContactExistsPage.toString)
      result.get.data.value must not contain key(SecondContactNamePage.toString)
      result.get.data.value must not contain key(SecondContactEmailPage.toString)
      result.get.data.value must not contain key(SecondContactCanWePhonePage.toString)
      result.get.data.value must not contain key(SecondContactPhoneNumberPage.toString)
    }

    "when true" in {
      val result = IsThisYourBusinessNamePage.cleanup(Some(true), userAnswersForAddFI)
      result.get.data.value must not contain key(NameOfFinancialInstitutionPage.toString)
      result.get.data.value must not contain key(HaveUniqueTaxpayerReferencePage.toString)
      result.get.data.value must not contain key(WhatIsUniqueTaxpayerReferencePage.toString)
      result.get.data.value must not contain key(FirstContactNamePage.toString)
      result.get.data.value must not contain key(FirstContactEmailPage.toString)
      result.get.data.value must not contain key(FirstContactHavePhonePage.toString)
      result.get.data.value must not contain key(FirstContactPhoneNumberPage.toString)
      result.get.data.value must not contain key(SecondContactExistsPage.toString)
      result.get.data.value must not contain key(SecondContactNamePage.toString)
      result.get.data.value must not contain key(SecondContactEmailPage.toString)
      result.get.data.value must not contain key(SecondContactCanWePhonePage.toString)
      result.get.data.value must not contain key(SecondContactPhoneNumberPage.toString)
    }
  }

}
