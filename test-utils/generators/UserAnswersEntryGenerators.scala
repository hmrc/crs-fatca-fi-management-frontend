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

import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsValue, Json, Writes}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryIsTheAddressCorrectUserAnswersEntry
    : Arbitrary[(pages.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectPage.type, Boolean](
      pages.addFinancialInstitution.IsRegisteredBusiness.IsTheAddressCorrectPage
    )

  implicit lazy val arbitraryReportForRegisteredBusinessUserAnswersEntry
    : Arbitrary[(pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage.type, Boolean](
      pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
    )

  implicit lazy val arbitraryIsThisYourBusinessNameUserAnswersEntry
    : Arbitrary[(pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage.type, Boolean](
      pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage
    )

  implicit lazy val arbitraryFirstContactEmailUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.FirstContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.FirstContactEmailPage)

  implicit lazy val arbitraryFirstContactHavePhoneUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.FirstContactHavePhonePage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.FirstContactHavePhonePage.type, Boolean](pages.addFinancialInstitution.FirstContactHavePhonePage)

  implicit lazy val arbitraryFirstContactPhoneNumberUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.FirstContactPhoneNumberPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.FirstContactPhoneNumberPage)

  implicit lazy val arbitraryFirstContactNameUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.FirstContactNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.FirstContactNamePage)

  implicit lazy val arbitraryHaveGIINPageUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.HaveGIINPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.HaveGIINPage.type, Boolean](pages.addFinancialInstitution.HaveGIINPage)

  implicit lazy val arbitraryIsThisAddressUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.IsThisAddressPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.IsThisAddressPage.type, Boolean](pages.addFinancialInstitution.IsThisAddressPage)

  implicit lazy val arbitraryNameOfFinancialInstitutionUserAnswersEntry
    : Arbitrary[(pages.addFinancialInstitution.NameOfFinancialInstitutionPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.NameOfFinancialInstitutionPage)

  implicit lazy val arbitraryNonUkAddressPageEntry: Arbitrary[(pages.addFinancialInstitution.NonUkAddressPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.NonUkAddressPage.type, models.Address](pages.addFinancialInstitution.NonUkAddressPage)

  implicit lazy val arbitraryPostcodePageUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.PostcodePage.type, JsValue)] =
    alphaStrPageArbitrary(pages.addFinancialInstitution.PostcodePage)

  implicit lazy val arbitrarySecondContactCanWePhoneUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SecondContactCanWePhonePage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.SecondContactCanWePhonePage.type, Boolean](pages.addFinancialInstitution.SecondContactCanWePhonePage)

  implicit lazy val arbitrarySecondContactEmailUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SecondContactEmailPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.SecondContactEmailPage)

  implicit lazy val arbitrarySecondContactExistsUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SecondContactExistsPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.SecondContactExistsPage.type, Boolean](pages.addFinancialInstitution.SecondContactExistsPage)

  implicit lazy val arbitrarySecondContactNameUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SecondContactNamePage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.SecondContactNamePage)

  implicit lazy val arbitrarySecondContactPhoneNumberUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SecondContactPhoneNumberPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.SecondContactPhoneNumberPage)

  implicit lazy val arbitrarySelectAddressUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SelectAddressPage.type, JsValue)] =
    alphaStrNonEmptyPageArbitrary(pages.addFinancialInstitution.SelectAddressPage)

  implicit lazy val arbitraryAddressLookupUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.SelectedAddressLookupPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.SelectedAddressLookupPage.type, models.AddressLookup](pages.addFinancialInstitution.SelectedAddressLookupPage)

  implicit lazy val arbitraryUkAddressEntry: Arbitrary[(pages.addFinancialInstitution.UkAddressPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.UkAddressPage.type, models.Address](pages.addFinancialInstitution.UkAddressPage)

  implicit lazy val arbitraryWhatIsGIINUserAnswersEntry: Arbitrary[(pages.addFinancialInstitution.WhatIsGIINPage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.WhatIsGIINPage.type, models.GIINumber](pages.addFinancialInstitution.WhatIsGIINPage)

  implicit lazy val arbitraryWhatIsUniqueTaxpayerReferenceEntry: Arbitrary[(pages.addFinancialInstitution.WhatIsUniqueTaxpayerReferencePage.type, JsValue)] =
    modelArbitrary[pages.addFinancialInstitution.WhatIsUniqueTaxpayerReferencePage.type, models.UniqueTaxpayerReference](
      pages.addFinancialInstitution.WhatIsUniqueTaxpayerReferencePage
    )

  private def alphaStrNonEmptyPageArbitrary[T](page: T): Arbitrary[(T, JsValue)] = Arbitrary {
    for {
      value <- Gen.alphaStr.suchThat(_.nonEmpty).map(Json.toJson(_))
    } yield (page, value)
  }

  private def alphaStrPageArbitrary[T](page: T): Arbitrary[(T, JsValue)] = Arbitrary {
    for {
      value <- Gen.alphaStr.map(Json.toJson(_))
    } yield (page, value)
  }

  private def modelArbitrary[T, U](page: T)(implicit arb: Arbitrary[U], writes: Writes[U]): Arbitrary[(T, JsValue)] = Arbitrary {
    for {
      value <- arb.arbitrary.map(Json.toJson(_))
    } yield (page, value)
  }

}
