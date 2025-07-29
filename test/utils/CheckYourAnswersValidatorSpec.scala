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

package utils

import generators.{ModelGenerators, UserAnswersGenerator}
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.addFinancialInstitution.IsRegisteredBusiness.{
  FetchedRegisteredAddressPage,
  IsTheAddressCorrectPage,
  IsThisYourBusinessNamePage,
  ReportForRegisteredBusinessPage
}
import pages.addFinancialInstitution._

class CheckYourAnswersValidatorSpec extends AnyFreeSpec with Matchers with ModelGenerators with UserAnswersGenerator with ScalaCheckPropertyChecks {

  "CheckYourAnswersValidator" - {

    "not registered add FI journey" - {

      "return an empty list if no answers are missing" in {
        forAll(fiNotRegistered.arbitrary) {
          (userAnswers: UserAnswers) =>
            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustBe Nil
        }
      }

      "return missing answers" in {
        forAll(fiNotRegisteredMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            val result = CheckYourAnswersValidator(userAnswers).validate
            result mustNot be(empty)
            Set(
              NameOfFinancialInstitutionPage,
              WhichIdentificationNumbersPage,
              FirstContactEmailPage,
              FirstContactHavePhonePage,
              FirstContactNamePage,
              FirstContactPhoneNumberPage,
              HaveGIINPage,
              IsThisAddressPage,
              SelectAddressPage,
              SelectedAddressLookupPage,
              NonUkAddressPage,
              UkAddressPage,
              WhatIsGIINPage,
              WhatIsUniqueTaxpayerReferencePage,
              SecondContactEmailPage,
              SecondContactExistsPage,
              SecondContactNamePage,
              SecondContactPhoneNumberPage
            ) must contain allElementsOf result
        }
      }

      "return correct redirect url" in {
        forAll(fiNotRegisteredMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            val result = CheckYourAnswersValidator(userAnswers).changeAnswersRedirectUrl
            Set(
              controllers.addFinancialInstitution.routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url,
              controllers.addFinancialInstitution.routes.HaveGIINController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.FirstContactHavePhoneController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.SecondContactCanWePhoneController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.SecondContactEmailController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.SecondContactExistsController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.WhichIdentificationNumbersController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.WhatIsCompanyRegistrationNumberController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.TrustURNController.onPageLoad(CheckMode).url
            ) must contain(result)
        }
      }

    }
    "registered add FI journey" - {

      "return an empty list if no answers are missing" in {
        forAll(fiRegistered.arbitrary) {
          (userAnswers: UserAnswers) =>
            val result = CheckYourAnswersValidator(userAnswers).validateRegisteredBusiness
            result mustBe Nil
        }
      }

      "return missing answers" in {
        forAll(fiRegisteredMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            val result = CheckYourAnswersValidator(userAnswers).validateRegisteredBusiness
            result mustNot be(empty)
            Set(
              HaveGIINPage,
              WhatIsGIINPage,
              ReportForRegisteredBusinessPage,
              IsThisYourBusinessNamePage,
              IsTheAddressCorrectPage,
              FetchedRegisteredAddressPage
            ) must contain allElementsOf result
        }
      }

      "return correct redirect url" in {
        forAll(fiRegisteredMissingAnswers.arbitrary) {
          (userAnswers: UserAnswers) =>
            val result = CheckYourAnswersValidator(userAnswers).changeAnswersRedirectUrlForRegisteredBusiness
            Set(
              controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.routes.HaveGIINController.onPageLoad(CheckMode).url,
              controllers.addFinancialInstitution.registeredBusiness.routes.IsThisYourBusinessNameController.onPageLoad(CheckMode).url
            ) must contain(result)
        }
      }
    }
  }

}
