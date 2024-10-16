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
import models.{CheckMode, _}
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages._
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId

class CheckModeNavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator in Check mode" - {
    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        CheckMode,
        UserAnswers("id")
      ) mustBe routes.CheckYourAnswersController.onPageLoad
    }

    "when adding an FI" - {
      // Fields that only appear on /check-answers:
      // FIName in standard
      "must go from NameOfFinancialInstitution to CheckAnswers when No" in {
        val userAnswers = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, "fiName")
        navigator.nextPage(NameOfFinancialInstitutionPage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      // UTR
      "must go from HaveUniqueTaxpayerReference to CheckAnswers when No" in {
        val userAnswers = emptyUserAnswers.withPage(HaveUniqueTaxpayerReferencePage, false)
        navigator.nextPage(HaveUniqueTaxpayerReferencePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from HaveUniqueTaxpayerReference to WhatIsGIIN when Yes" in {
        val userAnswers = emptyUserAnswers.withPage(HaveUniqueTaxpayerReferencePage, true)
        navigator.nextPage(HaveUniqueTaxpayerReferencePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(CheckMode)
      }
      "must go from WhatIsUniqueTaxpayerReference to CheckAnswers" in {
        val userAnswers = emptyUserAnswers.withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("someUTR"))
        navigator.nextPage(WhatIsUniqueTaxpayerReferencePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      // Contact rows, first contact
      "must go from FirstContactName to CheckAnswers" in {
        val userAnswers = emptyUserAnswers.withPage(FirstContactNamePage, "testname")
        navigator.nextPage(FirstContactNamePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from FirstContactEmail to CheckAnswers" in {
        val userAnswers = emptyUserAnswers.withPage(FirstContactEmailPage, "test@email.io")
        navigator.nextPage(FirstContactNamePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from FirstContactHavePhone to CheckAnswers when No" in {
        val userAnswers = emptyUserAnswers.withPage(FirstContactHavePhonePage, false)
        navigator.nextPage(FirstContactHavePhonePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from FirstContactHavePhone to FirstContactPhoneNumber when Yes" in {
        val userAnswers = emptyUserAnswers.withPage(FirstContactHavePhonePage, true)
        navigator.nextPage(FirstContactHavePhonePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.FirstContactPhoneNumberController.onPageLoad(CheckMode)
      }
      "must go from FirstContactPhoneNumber to CheckAnswers" in {
        val userAnswers = emptyUserAnswers.withPage(FirstContactPhoneNumberPage, "07123456789")
        navigator.nextPage(FirstContactPhoneNumberPage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      // second contact
      "must go from SecondContactExists to CheckAnswers when No" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactExistsPage, false)
        navigator.nextPage(SecondContactExistsPage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "when SecondContactExists is changed from no to yes" - {
        "must go from SecondContactExists to SecondContactName when yes" in {
          val userAnswers = emptyUserAnswers.withPage(SecondContactExistsPage, true)
          navigator.nextPage(SecondContactExistsPage, CheckMode, userAnswers) mustBe
            controllers.addFinancialInstitution.routes.SecondContactNameController.onPageLoad(CheckMode)
        }
        "must go from SecondContactName to SecondContactEmail" in {
          val userAnswers = emptyUserAnswers.withPage(SecondContactNamePage, "name")
          navigator.nextPage(SecondContactNamePage, CheckMode, userAnswers) mustBe
            controllers.addFinancialInstitution.routes.SecondContactEmailController.onPageLoad(CheckMode)
        }
        "must go from SecondContactEmail to SecondContactCanWePhone" in {
          val userAnswers = emptyUserAnswers
            .withPage(SecondContactEmailPage, "name@email.com")
          navigator.nextPage(SecondContactEmailPage, CheckMode, userAnswers) mustBe
            controllers.addFinancialInstitution.routes.SecondContactCanWePhoneController.onPageLoad(CheckMode)
        }

      }
      "must go from SecondContactName to CheckAnswers" in {
        val userAnswers = emptyUserAnswers
          .withPage(SecondContactNamePage, "testname")
          .withPage(SecondContactEmailPage, "test@email.io")
        navigator.nextPage(SecondContactNamePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from SecondContactEmail to CheckAnswers" in {
        val userAnswers = emptyUserAnswers
          .withPage(SecondContactEmailPage, "test@email.io")
          .withPage(SecondContactCanWePhonePage, false)
        navigator.nextPage(SecondContactNamePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from SecondContactCanWePhone to CheckAnswers when No" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactCanWePhonePage, false)
        navigator.nextPage(SecondContactCanWePhonePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "must go from SecondContactCanWePhone to SecondContactPhoneNumber when Yes" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactCanWePhonePage, true)
        navigator.nextPage(SecondContactCanWePhonePage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.SecondContactPhoneNumberController.onPageLoad(CheckMode)
      }
      "must go from SecondContactPhoneNumber to CheckAnswers" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactPhoneNumberPage, "07123456789")
        navigator.nextPage(SecondContactPhoneNumberPage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
    }
    "must go from ReportForRegisteredBusinessPage" - {
      "to RegisteredBusinessCheckYourAnswers when Yes" in {
        val userAnswers = emptyUserAnswers.withPage(ReportForRegisteredBusinessPage, true)
        navigator.nextPage(ReportForRegisteredBusinessPage, CheckMode, userAnswers) mustBe
          controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
      }
      "to manage FIs home page when No" in {
        val userAnswers = emptyUserAnswers.withPage(ReportForRegisteredBusinessPage, false)
        navigator.nextPage(ReportForRegisteredBusinessPage, CheckMode, userAnswers) mustBe
          controllers.routes.IndexController.onPageLoad()
      }
    }
    // FI name
    "must go from IsThisYourBusinessName" - {
      "to CheckAnswers when Yes" in {
        val ua = emptyUserAnswers.withPage(IsThisYourBusinessNamePage, true)
        navigator.nextPage(IsThisYourBusinessNamePage, CheckMode, ua) mustBe controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad()
      }
      "to NameOfFinancialInstitutionPage when No" in {
        val ua = emptyUserAnswers.withPage(IsThisYourBusinessNamePage, false)
        navigator.nextPage(IsThisYourBusinessNamePage, CheckMode, ua) mustBe routes.NameOfFinancialInstitutionController.onPageLoad(CheckMode)
      }
    }
    // Fields that are shared by both Answers Pages:
    "when adding user as an FI" - {
      val userIsFiStatus = Table(
        ("userAnswers", "CYA", "isFIUser"),
        (UserAnswers("id").withPage(ReportForRegisteredBusinessPage, false),
         controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad(),
         ""
        ),
        (UserAnswers("id").withPage(ReportForRegisteredBusinessPage, true),
         controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad(),
         "when reporting for registered business"
        )
      )
      forAll(userIsFiStatus) {
        (userAnswers, CYA, isFIUser) =>
          // GIIN
          s"must go from HaveGIIN $isFIUser" - {
            s"to Answers page" in {
              val ua = userAnswers.withPage(HaveGIINPage, false)
              navigator.nextPage(HaveGIINPage, CheckMode, ua) mustBe CYA
            }
            "to WhatIsGIIN when Yes" in {
              val ua = userAnswers.withPage(HaveGIINPage, true)
              navigator.nextPage(HaveGIINPage, CheckMode, ua) mustBe
                controllers.addFinancialInstitution.routes.WhatIsGIINController.onPageLoad(CheckMode)
            }
          }
          s"must go from WhatIsGIIN to Answers page $isFIUser" in {
            val ua = userAnswers.withPage(WhatIsGIINPage, GIINumber("someGIIN"))
            navigator.nextPage(WhatIsGIINPage, CheckMode, ua) mustBe CYA
          }

          // Addresses
          s"must go from IsTheAddressCorrect to Answers page $isFIUser" in {
            val ua = userAnswers.withPage(IsTheAddressCorrectPage, true)
            navigator.nextPage(IsTheAddressCorrectPage, CheckMode, ua) mustBe CYA
          }
          s"must go from IsTheAddressCorrect to WhereIsFIBased when No $isFIUser" in {
            val ua = userAnswers.withPage(IsTheAddressCorrectPage, false)
            navigator.nextPage(IsTheAddressCorrectPage, CheckMode, ua) mustBe
              routes.WhereIsFIBasedController.onPageLoad(CheckMode)
          }

          s"must go from WhereIsFIBased $isFIUser" - {
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
          s"must go from Postcode page $isFIUser" - {
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
          s"must go from IsThisAddress $isFIUser" - {
            "to Answers page when Yes" in {
              val ua = userAnswers
                .withPage(IsThisAddressPage, true)
              navigator.nextPage(IsThisAddressPage, CheckMode, ua) mustBe CYA
            }
            "to UkAddress page when No " in {
              val ua = userAnswers
                .withPage(IsThisAddressPage, false)
              navigator.nextPage(IsThisAddressPage, CheckMode, ua) mustBe
                routes.UkAddressController.onPageLoad(CheckMode)
            }
          }
          s"must return to Answers page $isFIUser" - {
            "from NonUkAddress" in {
              navigator.nextPage(NonUkAddressPage, CheckMode, userAnswers) mustBe CYA
            }
            "from UkAddress" in {
              navigator.nextPage(UkAddressPage, CheckMode, userAnswers) mustBe CYA
            }
            "from SelectAddress" in {
              navigator.nextPage(SelectAddressPage, CheckMode, userAnswers) mustBe CYA
            }
          }
      }

      // Change Details
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

  }

}
