/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import org.scalacheck.Arbitrary

trait PageGenerators {

  implicit lazy val arbitraryFetchedRegisteredAddressPage: Arbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage.type] =
    Arbitrary(pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage)

  implicit lazy val arbitraryIsTheAddressCorrectPage: Arbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectPage.type] =
    Arbitrary(pages.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectPage)

  implicit lazy val arbitraryIsThisYourBusinessNamePage: Arbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage.type] =
    Arbitrary(pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage)

  implicit lazy val arbitraryReportForRegisteredBusinessPage
    : Arbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage.type] =
    Arbitrary(pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage)

  implicit lazy val arbitraryFirstContactEmailPage: Arbitrary[pages.addFinancialInstitution.FirstContactEmailPage.type] =
    Arbitrary(pages.addFinancialInstitution.FirstContactEmailPage)

  implicit lazy val arbitraryFirstContactHavePhonePage: Arbitrary[pages.addFinancialInstitution.FirstContactHavePhonePage.type] =
    Arbitrary(pages.addFinancialInstitution.FirstContactHavePhonePage)

  implicit lazy val arbitraryFirstContactNamePage: Arbitrary[pages.addFinancialInstitution.FirstContactNamePage.type] =
    Arbitrary(pages.addFinancialInstitution.FirstContactNamePage)

  implicit lazy val arbitraryFirstContactPhoneNumberPage: Arbitrary[pages.addFinancialInstitution.FirstContactPhoneNumberPage.type] =
    Arbitrary(pages.addFinancialInstitution.FirstContactPhoneNumberPage)

  implicit lazy val arbitraryHaveGIINPage: Arbitrary[pages.addFinancialInstitution.HaveGIINPage.type] =
    Arbitrary(pages.addFinancialInstitution.HaveGIINPage)

  implicit lazy val arbitraryIsThisAddressPage: Arbitrary[pages.addFinancialInstitution.IsThisAddressPage.type] =
    Arbitrary(pages.addFinancialInstitution.IsThisAddressPage)

  implicit lazy val arbitraryNameOfFinancialInstitutionPage: Arbitrary[pages.addFinancialInstitution.NameOfFinancialInstitutionPage.type] =
    Arbitrary(pages.addFinancialInstitution.NameOfFinancialInstitutionPage)

  implicit lazy val arbitraryNonUkAddressPage: Arbitrary[pages.addFinancialInstitution.NonUkAddressPage.type] =
    Arbitrary(pages.addFinancialInstitution.NonUkAddressPage)

  implicit lazy val arbitraryPostcodePage: Arbitrary[pages.addFinancialInstitution.PostcodePage.type] =
    Arbitrary(pages.addFinancialInstitution.PostcodePage)

  implicit lazy val arbitrarySecondContactCanWePhonePage: Arbitrary[pages.addFinancialInstitution.SecondContactCanWePhonePage.type] =
    Arbitrary(pages.addFinancialInstitution.SecondContactCanWePhonePage)

  implicit lazy val arbitrarySecondContactEmailPage: Arbitrary[pages.addFinancialInstitution.SecondContactEmailPage.type] =
    Arbitrary(pages.addFinancialInstitution.SecondContactEmailPage)

  implicit lazy val arbitrarySecondContactExistsPage: Arbitrary[pages.addFinancialInstitution.SecondContactExistsPage.type] =
    Arbitrary(pages.addFinancialInstitution.SecondContactExistsPage)

  implicit lazy val arbitrarySecondContactNamePage: Arbitrary[pages.addFinancialInstitution.SecondContactNamePage.type] =
    Arbitrary(pages.addFinancialInstitution.SecondContactNamePage)

  implicit lazy val arbitrarySecondContactPhoneNumberPage: Arbitrary[pages.addFinancialInstitution.SecondContactPhoneNumberPage.type] =
    Arbitrary(pages.addFinancialInstitution.SecondContactPhoneNumberPage)

  implicit lazy val arbitrarySelectAddressPage: Arbitrary[pages.addFinancialInstitution.SelectAddressPage.type] =
    Arbitrary(pages.addFinancialInstitution.SelectAddressPage)

  implicit lazy val arbitrarySelectedAddressLookupPage: Arbitrary[pages.addFinancialInstitution.SelectedAddressLookupPage.type] =
    Arbitrary(pages.addFinancialInstitution.SelectedAddressLookupPage)

  implicit lazy val arbitraryUkAddressPage: Arbitrary[pages.addFinancialInstitution.UkAddressPage.type] =
    Arbitrary(pages.addFinancialInstitution.UkAddressPage)

  implicit lazy val arbitraryWhatIsGIINPage: Arbitrary[pages.addFinancialInstitution.WhatIsGIINPage.type] =
    Arbitrary(pages.addFinancialInstitution.WhatIsGIINPage)

  implicit lazy val arbitraryWhatIsUniqueTaxpayerReferencePage: Arbitrary[pages.addFinancialInstitution.WhatIsUniqueTaxpayerReferencePage.type] =
    Arbitrary(pages.addFinancialInstitution.WhatIsUniqueTaxpayerReferencePage)

}
