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
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages._
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe controllers.routes.IndexController.onPageLoad
      }
      "must go from FirstContactName page to FirstContactEmail" in {
        navigator.nextPage(FirstContactNamePage, NormalMode, UserAnswers("id")) mustBe
          routes.FirstContactEmailController.onPageLoad(NormalMode)
      }
      "must go from FirstContactEmail page to FirstContactHavePhone" in {
        navigator.nextPage(FirstContactEmailPage, NormalMode, UserAnswers("id")) mustBe
          routes.FirstContactHavePhoneController.onPageLoad(NormalMode)
      }
      "must go from FirstContactHavePhonePage" - {
        " to FirstContactPhone if Yes" in {
          val userAnswers = emptyUserAnswers.set(FirstContactHavePhonePage, true).get
          navigator.nextPage(FirstContactHavePhonePage, NormalMode, userAnswers) mustBe
            routes.FirstContactPhoneNumberController.onPageLoad(NormalMode)
        }
        " to SecondContactExists if No" in {
          val userAnswers = emptyUserAnswers.set(FirstContactHavePhonePage, false).get
          navigator.nextPage(FirstContactHavePhonePage, NormalMode, userAnswers) mustBe
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

      "must go from NameOfFinancialInstitutionPage to HaveGIIN when user is FI" in {
        val userAnswers = emptyUserAnswers.withPage(ReportForRegisteredBusinessPage, true)
        navigator.nextPage(NameOfFinancialInstitutionPage, NormalMode, userAnswers) mustBe
          routes.HaveGIINController.onPageLoad(NormalMode)
      }

      "must go from NameOfFinancialInstitutionPage to HaveUniqueTaxpayerReferencePage when user is not FI" in {
        val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, "FI")
        navigator.nextPage(NameOfFinancialInstitutionPage, NormalMode, userAnswers) mustBe
          routes.HaveUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      }

      "IsThisAddress" - {
        "when user answers yes must go to" - {
          "RegisteredBusinessCheckYourAnswers when FI is the user" in {
            val userAnswers = emptyUserAnswers
              .withPage(IsThisAddressPage, true)
              .withPage(ReportForRegisteredBusinessPage, true)

            navigator.nextPage(IsThisAddressPage, NormalMode, userAnswers) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
          }
          "FirstContactName when FI is not the user" in {
            val userAnswers = emptyUserAnswers
              .withPage(IsThisAddressPage, true)

            navigator.nextPage(IsThisAddressPage, NormalMode, userAnswers) mustBe
              routes.FirstContactNameController.onPageLoad(NormalMode)
          }
        }
        "must go to UkAddress when user answers no" - {
          val userAnswers = emptyUserAnswers
            .withPage(IsThisAddressPage, false)

          navigator.nextPage(IsThisAddressPage, NormalMode, userAnswers) mustBe
            routes.UkAddressController.onPageLoad(NormalMode)
        }
      }

      "HaveGIIN" - {

        "must go to WhatIsGIIN when user answers yes" in {
          val userAnswers = emptyUserAnswers.withPage(HaveGIINPage, true)
          navigator.nextPage(HaveGIINPage, NormalMode, userAnswers) mustBe
            routes.WhatIsGIINController.onPageLoad(NormalMode)
        }

        "if the FI is the user" - {
          "must go to IsTheAddressCorrect when user answers no" in {
            val userAnswers = emptyUserAnswers
              .withPage(ReportForRegisteredBusinessPage, true)
              .withPage(HaveGIINPage, false)

            navigator.nextPage(HaveGIINPage, NormalMode, userAnswers) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(NormalMode)
          }
        }

        "if the FI is not the user" - {
          "must go to WhereIsFIBased when user answers no" in {
            val userAnswers = emptyUserAnswers
              .withPage(ReportForRegisteredBusinessPage, false)
              .withPage(HaveGIINPage, false)

            navigator.nextPage(HaveGIINPage, NormalMode, userAnswers) mustBe
              routes.WhereIsFIBasedController.onPageLoad(NormalMode)
          }
        }

      }

      "WhatIsGIIN" - {

        "must go to IsTheAddressCorrect when FI is the user" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReportForRegisteredBusinessPage, true)
            .withPage(WhatIsGIINPage, GIINumber("answer"))

          navigator.nextPage(WhatIsGIINPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(NormalMode)
        }

        "must go to WhereIsFIBased when FI is not the user" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReportForRegisteredBusinessPage, false)
            .withPage(WhatIsGIINPage, GIINumber("answer"))

          navigator.nextPage(WhatIsGIINPage, NormalMode, userAnswers) mustBe
            routes.WhereIsFIBasedController.onPageLoad(NormalMode)
        }
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

      "must go from UkAddress" - {
        "to RegisteredBusinessCheckYourAnswers when FI is the user" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReportForRegisteredBusinessPage, true)

          navigator.nextPage(UkAddressPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
        }
        "to FirstContactName when FI is not the user" in {
          navigator.nextPage(UkAddressPage, NormalMode, emptyUserAnswers) mustBe
            routes.FirstContactNameController.onPageLoad(NormalMode)
        }
      }

      "must go from NonUkAddress" - {
        "to RegisteredBusinessCheckYourAnswers when FI is the user" in {
          val userAnswers = emptyUserAnswers
            .withPage(ReportForRegisteredBusinessPage, true)

          navigator.nextPage(NonUkAddressPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
        }
        "to FirstContactName when FI is not the user" in {
          navigator.nextPage(NonUkAddressPage, NormalMode, emptyUserAnswers) mustBe
            routes.FirstContactNameController.onPageLoad(NormalMode)
        }
      }
      "must go from ReportForRegisteredBusiness" - {
        " to IsThisYourBusinessName if Yes" in {
          val userAnswers = emptyUserAnswers.set(ReportForRegisteredBusinessPage, true).get
          navigator.nextPage(ReportForRegisteredBusinessPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.IsThisYourBusinessNameController.onPageLoad(NormalMode)
        }
        " to NameOfFinancialInstitution if No" in {
          val userAnswers = emptyUserAnswers.set(ReportForRegisteredBusinessPage, false).get
          navigator.nextPage(ReportForRegisteredBusinessPage, NormalMode, userAnswers) mustBe
            routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
        }
      }

      "must go from IsThisYourBusinessName" - {
        " to HaveGIIN if Yes" in {
          val userAnswers = emptyUserAnswers.set(IsThisYourBusinessNamePage, true).get
          navigator.nextPage(IsThisYourBusinessNamePage, NormalMode, userAnswers) mustBe
            routes.HaveGIINController.onPageLoad(NormalMode)
        }
        " to NameOfFinancialInstitution if No" in {
          val userAnswers = emptyUserAnswers.set(ReportForRegisteredBusinessPage, false).get
          navigator.nextPage(ReportForRegisteredBusinessPage, NormalMode, userAnswers) mustBe
            routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
        }
      }

      "must go from IsTheAddressCorrect" - {
        " to RegisteredBusinessCheckYourAnswers if Yes" in {
          val userAnswers = emptyUserAnswers.set(IsTheAddressCorrectPage, true).get
          navigator.nextPage(IsTheAddressCorrectPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
        }
        " to WhereIsFiBased if No" in {
          val userAnswers = emptyUserAnswers.set(IsTheAddressCorrectPage, false).get
          navigator.nextPage(IsTheAddressCorrectPage, NormalMode, userAnswers) mustBe
            routes.WhereIsFIBasedController.onPageLoad(NormalMode)
        }
      }
      "must go from SelectAddress" - {
        "to RegisteredBusinessCheckYourAnswers when FI is the User" in {
          val userAnswers = emptyUserAnswers
            .withPage(SelectAddressPage, "someSelectedAddress")
            .withPage(ReportForRegisteredBusinessPage, true)

          navigator.nextPage(SelectAddressPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
        }
        "to FirstContactName when FI is not the User" in {
          val userAnswers = emptyUserAnswers
            .withPage(SelectAddressPage, "someSelectedAddress")

          navigator.nextPage(SelectAddressPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.routes.FirstContactNameController.onPageLoad(NormalMode)
        }
      }

    }

    "in Check mode" - {
      "when FI=USER" - {
        val userAnswers = UserAnswers("id").withPage(ReportForRegisteredBusinessPage, true)

        "must go from IsTheAddressCorrect" - {
          "to RegisteredBusinessCheckYourAnswers when Yes" in {
            val ua = userAnswers.withPage(IsTheAddressCorrectPage, true).withPage(ReportForRegisteredBusinessPage, true)
            navigator.nextPage(IsTheAddressCorrectPage, CheckMode, ua) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
          }
          "to WhereIsFIBased when No" in {
            val ua = userAnswers.withPage(IsTheAddressCorrectPage, false)
            navigator.nextPage(IsTheAddressCorrectPage, CheckMode, ua) mustBe
              routes.WhereIsFIBasedController.onPageLoad(CheckMode)
          }
        }
        "must go from WhereIsFIBased" - {
          "to Postcode page when Yes" in {
            val ua = userAnswers.withPage(WhereIsFIBasedPage, true)
            navigator.nextPage(WhereIsFIBasedPage, CheckMode, ua) mustBe
              routes.PostcodeController.onPageLoad(CheckMode)
          }
          "to NonUkAddress page when No " in {
            val ua = userAnswers.withPage(WhereIsFIBasedPage, false)
            navigator.nextPage(WhereIsFIBasedPage, CheckMode, ua) mustBe
              routes.NonUkAddressController.onPageLoad(CheckMode)
          }
        }
        "must go from Postcode page" - {
          "to SelectAddress when lookup returns 1 address" in {
            val ua = userAnswers.withPage(AddressLookupPage, Seq(testAddressLookup))
            navigator.nextPage(PostcodePage, CheckMode, ua) mustBe
              routes.IsThisAddressController.onPageLoad(CheckMode)
          }
          "to IsThisAddress page when lookup returns >1 addresses" in {
            val ua = userAnswers.withPage(AddressLookupPage, Seq(testAddressLookup, testAddressLookup))
            navigator.nextPage(PostcodePage, CheckMode, ua) mustBe
              routes.SelectAddressController.onPageLoad(CheckMode)
          }
        }
        "must go from IsThisAddress" - {
          "to RegisteredBusinessCheckYourAnswers page when Yes" in {
            val ua = userAnswers
              .withPage(IsThisAddressPage, true)
            navigator.nextPage(IsThisAddressPage, CheckMode, ua) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
          }
          "to UkAddress page when No " in {
            val ua = userAnswers
              .withPage(IsThisAddressPage, false)
            navigator.nextPage(IsThisAddressPage, CheckMode, ua) mustBe
              routes.UkAddressController.onPageLoad(CheckMode)
          }
        }
        "must return to RegisteredBusinessCheckYourAnswers from" - {
          "NonUkAddress" in {
            navigator.nextPage(NonUkAddressPage, CheckMode, userAnswers) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
          }
          "UkAddress" in {
            navigator.nextPage(UkAddressPage, CheckMode, userAnswers) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
          }
          "SelectAddress" in {
            navigator.nextPage(SelectAddressPage, CheckMode, userAnswers) mustBe
              controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
          }
        }
      }

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "when change FI details is in progress" - {

        "must navigate from NameOfFinancialInstitutionPage to ChangeFinancialInstitution" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(NameOfFinancialInstitutionPage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from WhatIsUniqueTaxpayerReferencePage to ChangeFinancialInstitution" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(WhatIsUniqueTaxpayerReferencePage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from WhatIsGIINPage to ChangeFinancialInstitution" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(WhatIsGIINPage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from FirstContactNamePage to ChangeFinancialInstitution" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(FirstContactNamePage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from FirstContactEmailPage to ChangeFinancialInstitution" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(FirstContactEmailPage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from FirstContactHavePhonePage to ChangeFinancialInstitution when user answers no" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(FirstContactHavePhonePage, false)

              navigator
                .nextPage(FirstContactHavePhonePage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from FirstContactHavePhonePage to FirstContactPhoneNumber when user answers yes" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(FirstContactHavePhonePage, true)

              navigator
                .nextPage(FirstContactHavePhonePage, CheckMode, userAnswers)
                .mustBe(routes.FirstContactPhoneNumberController.onPageLoad(CheckMode))
          }
        }

        "must navigate from SecondContactExistsPage to ChangeFinancialInstitution when user answers no" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(SecondContactExistsPage, false)

              navigator
                .nextPage(SecondContactExistsPage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from SecondContactExistsPage to SecondContactName when user answers yes" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(SecondContactExistsPage, true)

              navigator
                .nextPage(SecondContactExistsPage, CheckMode, userAnswers)
                .mustBe(routes.SecondContactNameController.onPageLoad(CheckMode))
          }
        }

        "must navigate from SecondContactNamePage to ChangeFinancialInstitution when SecondContactEmail already exists" in {
          forAll {
            (fiId: String, secondContactEmail: String) =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(SecondContactEmailPage, secondContactEmail)

              navigator
                .nextPage(SecondContactNamePage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from SecondContactNamePage to SecondContactEmail" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(SecondContactNamePage, CheckMode, userAnswers)
                .mustBe(routes.SecondContactEmailController.onPageLoad(CheckMode))
          }
        }

        "must navigate from SecondContactEmailPage to ChangeFinancialInstitution when SecondContactCanWePhonePage already exists" in {
          forAll {
            (fiId: String, havePhone: Boolean) =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(SecondContactCanWePhonePage, havePhone)

              navigator
                .nextPage(SecondContactEmailPage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from SecondContactCanWePhonePage to ChangeFinancialInstitution when user answers no" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers
                .withPage(ChangeFiDetailsInProgressId, fiId)
                .withPage(SecondContactCanWePhonePage, false)

              navigator
                .nextPage(SecondContactCanWePhonePage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }

        "must navigate from SecondContactPhoneNumberPage to ChangeFinancialInstitution" in {
          forAll {
            fiId: String =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiId)

              navigator
                .nextPage(SecondContactPhoneNumberPage, CheckMode, userAnswers)
                .mustBe(controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiId))
          }
        }
      }
    }

    "removeNavigation must" - {
      "navigate to YourFinancialInstitutions regardless of answer" in {
        navigator.nextPage(RemoveAreYouSurePage,
                           NormalMode,
                           UserAnswers("id").withPage(RemoveAreYouSurePage, true)
        ) mustBe controllers.routes.YourFinancialInstitutionsController.onPageLoad()
        navigator.nextPage(RemoveAreYouSurePage,
                           NormalMode,
                           UserAnswers("id").withPage(RemoveAreYouSurePage, false)
        ) mustBe controllers.routes.YourFinancialInstitutionsController.onPageLoad()
      }

    }
  }

}
