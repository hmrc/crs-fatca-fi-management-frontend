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

package navigation

import base.SpecBase
import controllers.addFinancialInstitution.routes
import pages.addFinancialInstitution._
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe controllers.routes.IndexController.onPageLoad
      }
      "must go from ContactName page to FirstContactEmail" in {
        navigator.nextPage(ContactNamePage, NormalMode, UserAnswers("id")) mustBe
          routes.FirstContactEmailController.onPageLoad(NormalMode)
      }
      "must go from FirstContactEmail page to FirstContactCanWePhone" in {
        navigator.nextPage(FirstContactEmailPage, NormalMode, UserAnswers("id")) mustBe
          routes.ContactHavePhoneController.onPageLoad(NormalMode)
      }
      "must go from FirstContactCanWePhone" - {
        " to FirstContactPhone if Yes" in {
          val userAnswers = emptyUserAnswers.set(ContactHavePhonePage, true).get
          navigator.nextPage(ContactHavePhonePage, NormalMode, userAnswers) mustBe
            routes.FirstContactPhoneNumberController.onPageLoad(NormalMode)
        }
        " to SecondContactExists if No" in {
          val userAnswers = emptyUserAnswers.set(ContactHavePhonePage, false).get
          navigator.nextPage(ContactHavePhonePage, NormalMode, userAnswers) mustBe
            routes.SecondContactExistsController.onPageLoad(NormalMode)
        }
      }
      "must go from FirstContactPhoneNumber to SecondContactExists" in {
        navigator.nextPage(FirstContactPhoneNumberPage, NormalMode, UserAnswers("id")) mustBe
          routes.SecondContactExistsController.onPageLoad(NormalMode)
      }
      "must go from SecondContactExists" - {
        " to SecondContactName if Yes" in {
          val userAnswers = emptyUserAnswers.set(SecondContactExistsPage, true).get
          navigator.nextPage(SecondContactExistsPage, NormalMode, userAnswers) mustBe
            routes.SecondContactNameController.onPageLoad(NormalMode)
        }
        " to CheckYourAnswers if No" in {
          val userAnswers = emptyUserAnswers.set(SecondContactExistsPage, false).get
          navigator.nextPage(SecondContactExistsPage, NormalMode, userAnswers) mustBe
            routes.CheckYourAnswersController.onPageLoad
        }
      }
      "must go from SecondContactName to SecondContactEmail" in {
        navigator.nextPage(SecondContactNamePage, NormalMode, UserAnswers("id")) mustBe
          routes.SecondContactEmailController.onPageLoad(NormalMode)
      }
      "must go from SecondContactEmail to SecondContactCanWePhone" in {
        navigator.nextPage(SecondContactEmailPage, NormalMode, UserAnswers("id")) mustBe
          routes.SecondContactCanWePhoneController.onPageLoad(NormalMode)
      }

      "must go from SecondContactCanWePhonePage" - {
        "SecondContactPhoneNumberPage when Yes" in {
          val userAnswers = emptyUserAnswers.set(SecondContactCanWePhonePage, true).get
          navigator.nextPage(SecondContactCanWePhonePage, NormalMode, userAnswers) mustBe
            routes.SecondContactPhoneNumberController.onPageLoad(NormalMode)
        }

        "to CheckYourAnswersPage when No" in {
          val userAnswers = emptyUserAnswers.set(SecondContactCanWePhonePage, false).get
          navigator.nextPage(SecondContactCanWePhonePage, NormalMode, userAnswers) mustBe
            routes.CheckYourAnswersController.onPageLoad
        }
      }
      "must go from SecondContactPhoneNumber to CheckYourAnswers" in {
        navigator.nextPage(SecondContactPhoneNumberPage, NormalMode, UserAnswers("id")) mustBe
          routes.CheckYourAnswersController.onPageLoad
      }

      "must go from SecondContactCanWePhonePage to SecondContactPhoneNumberPage when user answers yes" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactCanWePhonePage, true)
        navigator.nextPage(SecondContactCanWePhonePage, NormalMode, userAnswers) mustBe
          routes.SecondContactPhoneNumberController.onPageLoad(NormalMode)
      }

      "must go from SecondContactCanWePhonePage to CheckYourAnswersPage when user answers no" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactCanWePhonePage, false)
        navigator.nextPage(SecondContactCanWePhonePage, NormalMode, userAnswers) mustBe
          routes.CheckYourAnswersController.onPageLoad
      }

      "must go from NameOfFinancialInstitutionPage to HaveUniqueTaxpayerReferencePage" in {
        val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, "FI")
        navigator.nextPage(NameOfFinancialInstitutionPage, NormalMode, userAnswers) mustBe
          routes.HaveUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      }

      "must go from IsThisAddress page to ContactName page when user answers yes" in {
        val userAnswers = emptyUserAnswers.withPage(IsThisAddressPage, true)
        navigator.nextPage(IsThisAddressPage, NormalMode, userAnswers) mustBe
          routes.ContactNameController.onPageLoad(NormalMode)
      }

      "must go from IsThisAddress page to ukAddress page when user answers no" in {
        val userAnswers = emptyUserAnswers.withPage(IsThisAddressPage, false)
        navigator.nextPage(IsThisAddressPage, NormalMode, userAnswers) mustBe
          routes.UkAddressController.onPageLoad(NormalMode)
      }

      "must go from HaveGIIN to WhatIsGIIN when user answers yes" in {
        val userAnswers = emptyUserAnswers.withPage(HaveGIINPage, true)
        navigator.nextPage(HaveGIINPage, NormalMode, userAnswers) mustBe
          routes.WhatIsGIINController.onPageLoad(NormalMode)
      }

      "must go from HaveGIIN to WhereIsFIBased when user answer is no" in {
        val userAnswers = emptyUserAnswers.withPage(HaveGIINPage, false)
        navigator.nextPage(HaveGIINPage, NormalMode, userAnswers) mustBe
          routes.WhereIsFIBasedController.onPageLoad(NormalMode)
      }

      "must go from WhatIsGIIN to WhereIsFIBased when user answers yes" in {
        val userAnswers = emptyUserAnswers.withPage(WhatIsGIINPage, "answer")
        navigator.nextPage(WhatIsGIINPage, NormalMode, userAnswers) mustBe
          routes.WhereIsFIBasedController.onPageLoad(NormalMode)
      }

      "must go from WhereIsFIBased to UKPostcode when user answers yes" in {
        val userAnswers = emptyUserAnswers.withPage(WhereIsFIBasedPage, true)
        navigator.nextPage(WhereIsFIBasedPage, NormalMode, userAnswers) mustBe
          routes.PostcodeController.onPageLoad(NormalMode)
      }

      "must go from WhereIsFIBased to NonUKAddress when user answers no" in {
        val userAnswers = emptyUserAnswers.withPage(WhereIsFIBasedPage, false)
        navigator.nextPage(WhereIsFIBasedPage, NormalMode, userAnswers) mustBe
          routes.NonUkAddressController.onPageLoad(NormalMode)
      }

      "must go from UkAddress to ContactName" in {
        navigator.nextPage(UkAddressPage, NormalMode, emptyUserAnswers) mustBe
          routes.ContactNameController.onPageLoad(NormalMode)
      }

      "must go from NonUkAddress to ContactName" in {
        navigator.nextPage(NonUkAddressPage, NormalMode, emptyUserAnswers) mustBe
          routes.ContactNameController.onPageLoad(NormalMode)
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }

}
