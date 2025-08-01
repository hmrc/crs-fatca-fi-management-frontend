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
import models.FinancialInstitutions.TINType
import models.FinancialInstitutions.TINType._
import models._
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
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe controllers.routes.IndexController.onPageLoad()
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
        " to CheckYourAnswers if No with No ChangeFIInProgress" in {
          val userAnswers = emptyUserAnswers.set(SecondContactExistsPage, false).get
          navigator.nextPage(SecondContactExistsPage, NormalMode, userAnswers) mustBe
            routes.CheckYourAnswersController.onPageLoad
        }
        " to ChangeYourAnswerPage if No with ChangeFIInProgress" in {
          val userAnswers    = emptyUserAnswers.set(SecondContactExistsPage, false).get
          val id             = "1234556"
          val updatedAnswers = userAnswers.set(ChangeFiDetailsInProgressId, id).get
          navigator.nextPage(SecondContactExistsPage, NormalMode, updatedAnswers) mustBe
            controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(id)
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

        "to CheckYourAnswersPage when No With No ChangeFIIdInProgress" in {
          val userAnswers = emptyUserAnswers.set(SecondContactCanWePhonePage, false).get
          navigator.nextPage(SecondContactCanWePhonePage, NormalMode, userAnswers) mustBe
            routes.CheckYourAnswersController.onPageLoad
        }

        "to ChangeYourAnswerPage when No With ChangeFIIdInProgress" in {
          val userAnswers    = emptyUserAnswers.set(SecondContactCanWePhonePage, false).get
          val id             = "1234556"
          val updatedAnswers = userAnswers.set(ChangeFiDetailsInProgressId, id).get
          navigator.nextPage(SecondContactCanWePhonePage, NormalMode, updatedAnswers) mustBe
            controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(id)
        }
      }
      "must go from SecondContactPhoneNumber to CheckYourAnswers When No ChangeFIInProgress" in {
        navigator.nextPage(SecondContactPhoneNumberPage, NormalMode, UserAnswers("id")) mustBe
          routes.CheckYourAnswersController.onPageLoad
      }

      "must go from SecondContactPhoneNumber to ChangeYourAnswer When ChangeFIInprogress" in {
        val id             = "1234556"
        val updatedAnswers = UserAnswers("id").set(ChangeFiDetailsInProgressId, id).get
        navigator.nextPage(SecondContactPhoneNumberPage, NormalMode, updatedAnswers) mustBe
          controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(id)
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

      "must go from SecondContactCanWePhonePage to ChangeYourAnswersPage when user answers no And ChangeFIInProgress" in {
        val userAnswers   = emptyUserAnswers.withPage(SecondContactCanWePhonePage, false)
        val id            = "12345678"
        val updatedAnswer = userAnswers.set(ChangeFiDetailsInProgressId, id).get
        navigator.nextPage(SecondContactCanWePhonePage, NormalMode, updatedAnswer) mustBe
          controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(id)
      }

      "must go from NameOfFinancialInstitutionPage to HaveGIIN when user is FI" in {
        val userAnswers = emptyUserAnswers.withPage(ReportForRegisteredBusinessPage, true)
        navigator.nextPage(NameOfFinancialInstitutionPage, NormalMode, userAnswers) mustBe
          routes.HaveGIINController.onPageLoad(NormalMode)
      }

      "must go from NameOfFinancialInstitutionPage to WhichIdentificationNumbersPage when user is not FI" in {
        val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, "FI")
        navigator.nextPage(NameOfFinancialInstitutionPage, NormalMode, userAnswers) mustBe
          routes.WhichIdentificationNumbersController.onPageLoad(NormalMode)
      }

      "must go from WhichIdentificationNumbersPage " - {
        "to WhatIsUniqueTaxpayerReference page when UTR selected" in {
          val userAnswers = emptyUserAnswers.withPage(WhichIdentificationNumbersPage, Set(UTR: TINType))
          navigator.nextPage(WhichIdentificationNumbersPage, NormalMode, userAnswers) mustBe
            routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
        }
        "to WhatIsUniqueTaxpayerReference page when both UTR and CRN are selected" in {
          val userAnswers = emptyUserAnswers.withPage(WhichIdentificationNumbersPage, Set(UTR: TINType, CRN: TINType))
          navigator.nextPage(WhichIdentificationNumbersPage, NormalMode, userAnswers) mustBe
            routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
        }
        "to WhatIsCompanyRegistrationNumber page when CRN selected" in {
          val userAnswers = emptyUserAnswers.withPage(WhichIdentificationNumbersPage, Set(CRN: TINType))
          navigator.nextPage(WhichIdentificationNumbersPage, NormalMode, userAnswers) mustBe
            routes.WhatIsCompanyRegistrationNumberController.onPageLoad(NormalMode)
        }
        "to TrustURN page when TRN selected" in {
          val userAnswers = emptyUserAnswers.withPage(WhichIdentificationNumbersPage, Set(TURN: TINType))
          navigator.nextPage(WhichIdentificationNumbersPage, NormalMode, userAnswers) mustBe
            routes.TrustURNController.onPageLoad(NormalMode)
        }

      }
      "must go from WhatIsUniqueTaxpayerReference" - {
        "to WhatIsCompanyRegistrationNumber if CRN was also selected" in {
          val userAnswers = emptyUserAnswers.withPage(WhichIdentificationNumbersPage, Set(UTR: TINType, CRN: TINType))
          navigator.nextPage(WhatIsUniqueTaxpayerReferencePage, NormalMode, userAnswers) mustBe
            routes.WhatIsCompanyRegistrationNumberController.onPageLoad(NormalMode)
        }
        "to HaveGIIN if only UTR was selected" in {
          val userAnswers = emptyUserAnswers.withPage(WhichIdentificationNumbersPage, Set(UTR: TINType))
          navigator.nextPage(WhatIsUniqueTaxpayerReferencePage, NormalMode, userAnswers) mustBe
            routes.HaveGIINController.onPageLoad(NormalMode)
        }
      }
      "must go from CompanyRegistrationNumber page to HaveGIIN page" in {
        navigator.nextPage(CompanyRegistrationNumberPage, NormalMode, UserAnswers("id")) mustBe
          routes.HaveGIINController.onPageLoad(NormalMode)
      }
      "must go from TrustURN page to HaveGIIN page" in {
        navigator.nextPage(TrustURNPage, NormalMode, UserAnswers("id")) mustBe
          routes.HaveGIINController.onPageLoad(NormalMode)
      }

      "IsThisAddress" - {
        "when user answers yes must go to" - {
          "RegisteredBusinessCheckYourAnswers when FI is the user" in {
            val userAnswers = emptyUserAnswers
              .withPage(ReportForRegisteredBusinessPage, true)
              .withPage(IsThisAddressPage, true)

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
              routes.PostcodeController.onPageLoad(NormalMode)
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
            routes.PostcodeController.onPageLoad(NormalMode)
        }
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
        " to RegisteredBusinessCheckYourAnswers if Yes with No ChangeFIInProgress" in {
          val userAnswers = emptyUserAnswers.set(IsTheAddressCorrectPage, true).get
          navigator.nextPage(IsTheAddressCorrectPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
        }
        " to RegisteredBusinessCheckYourAnswers if Yes with ChangeFIInProgress" in {
          val userAnswers    = emptyUserAnswers.set(IsTheAddressCorrectPage, true).get
          val id             = "12345678"
          val updatedAnswers = userAnswers.set(ChangeFiDetailsInProgressId, id).get
          navigator.nextPage(IsTheAddressCorrectPage, NormalMode, updatedAnswers) mustBe
            controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(id)
        }
        " to WhereIsFiBased if No" in {
          val userAnswers = emptyUserAnswers.set(IsTheAddressCorrectPage, false).get
          navigator.nextPage(IsTheAddressCorrectPage, NormalMode, userAnswers) mustBe
            routes.PostcodeController.onPageLoad(NormalMode)
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

        "to RegisteredBusinessCheckYourAnswers when FI is the User And ChangeInProgress" in {
          val id = "12345678"
          val userAnswers = emptyUserAnswers
            .withPage(SelectAddressPage, "someSelectedAddress")
            .withPage(ReportForRegisteredBusinessPage, true)
            .withPage(ChangeFiDetailsInProgressId, id)

          navigator.nextPage(SelectAddressPage, NormalMode, userAnswers) mustBe
            controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(id)
        }
        "to FirstContactName when FI is not the User" in {
          val userAnswers = emptyUserAnswers
            .withPage(SelectAddressPage, "someSelectedAddress")

          navigator.nextPage(SelectAddressPage, NormalMode, userAnswers) mustBe
            controllers.addFinancialInstitution.routes.FirstContactNameController.onPageLoad(NormalMode)
        }
      }

    }

    "removeNavigation must" - {
      "navigate to YourFinancialInstitutions regardless of answer" in {
        navigator.nextPage(RemoveAreYouSurePage,
                           NormalMode,
                           UserAnswers("id").withPage(RemoveAreYouSurePage, true)
        ) mustBe controllers.routes.FIRemovedController.onPageLoad()
        navigator.nextPage(RemoveAreYouSurePage,
                           NormalMode,
                           UserAnswers("id").withPage(RemoveAreYouSurePage, false)
        ) mustBe controllers.routes.YourFinancialInstitutionsController.onPageLoad()
      }

    }
  }

}
