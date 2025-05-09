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

class ReportForRegisteredBusinessPageSpec extends PageBehaviours {

  "ReportForRegisteredBusinessPage" - {
    beRetrievable[Boolean](ReportForRegisteredBusinessPage)

    beSettable[Boolean](ReportForRegisteredBusinessPage)

    beRemovable[Boolean](ReportForRegisteredBusinessPage)
  }

  "cleanup" - {

    "when true with selectedAddressLookupPage" in {
      val result = ReportForRegisteredBusinessPage.cleanup(Some(true), userAnswersForAddFI)
      result.get.data.keys must contain allElementsOf List(
        HaveGIINPage.toString,
        WhatIsGIINPage.toString
      )
      result.get.data.keys must contain noElementsOf List(
        WhatIsUniqueTaxpayerReferencePage.toString,
        FirstContactNamePage.toString,
        FirstContactEmailPage.toString,
        FirstContactHavePhonePage.toString,
        FirstContactPhoneNumberPage.toString,
        SecondContactExistsPage.toString,
        SecondContactNamePage.toString,
        SecondContactEmailPage.toString,
        SecondContactCanWePhonePage.toString,
        SecondContactPhoneNumberPage.toString,
        SelectedAddressLookupPage.toString,
        IsTheAddressCorrectPage.toString
      )
    }
    "when true with fetchedAddress" in {
      val userAnswersExtension = userAnswersForAddFI
        .remove(SelectedAddressLookupPage)
        .success
        .value
        .withPage(IsTheAddressCorrectPage, true)
        .withPage(FetchedRegisteredAddressPage, testAddressResponse)
      val result = ReportForRegisteredBusinessPage.cleanup(Some(true), userAnswersExtension)
      result.get.data.keys must contain allElementsOf List(
        HaveGIINPage.toString,
        WhatIsGIINPage.toString,
        IsTheAddressCorrectPage.toString,
        FetchedRegisteredAddressPage.toString
      )
      result.get.data.keys must contain noElementsOf List(
        WhatIsUniqueTaxpayerReferencePage.toString,
        FirstContactNamePage.toString,
        FirstContactEmailPage.toString,
        FirstContactHavePhonePage.toString,
        FirstContactPhoneNumberPage.toString,
        SecondContactExistsPage.toString,
        SecondContactNamePage.toString,
        SecondContactEmailPage.toString,
        SecondContactCanWePhonePage.toString,
        SecondContactPhoneNumberPage.toString
      )
    }

    "when true with ukAddress" in {
      val userAnswersExtension = userAnswersForAddFI
        .remove(SelectedAddressLookupPage)
        .success
        .value
        .remove(IsTheAddressCorrectPage)
        .success
        .value
        .withPage(UkAddressPage, testAddress)
        .withPage(PostcodePage, "ZZ11ZZ")
      val result = ReportForRegisteredBusinessPage.cleanup(Some(true), userAnswersExtension)
      result.get.data.keys must contain allElementsOf List(
        HaveGIINPage.toString,
        WhatIsGIINPage.toString
      )
      result.get.data.keys must contain noElementsOf List(
        WhatIsUniqueTaxpayerReferencePage.toString,
        FirstContactNamePage.toString,
        FirstContactEmailPage.toString,
        FirstContactHavePhonePage.toString,
        FirstContactPhoneNumberPage.toString,
        SecondContactExistsPage.toString,
        SecondContactNamePage.toString,
        SecondContactEmailPage.toString,
        SecondContactCanWePhonePage.toString,
        SecondContactPhoneNumberPage.toString,
        UkAddressPage.toString,
        PostcodePage.toString
      )
    }
    "when false" in {
      val result = ReportForRegisteredBusinessPage.cleanup(Some(false), userAnswersForAddUserAsFI)
      result.get.data.keys must contain noElementsOf List(
        IsThisYourBusinessNamePage.toString,
        IsTheAddressCorrectPage.toString,
        FetchedRegisteredAddressPage.toString
      )
    }
  }

}
