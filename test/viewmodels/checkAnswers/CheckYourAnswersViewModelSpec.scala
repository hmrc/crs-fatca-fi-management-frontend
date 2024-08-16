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

package viewmodels.checkAnswers

import base.SpecBase
import models.AddressResponse
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import play.api.i18n.Messages

class CheckYourAnswersViewModelSpec extends SpecBase {

  implicit val mockMessages: Messages = mock[Messages]
  val sut                             = CheckYourAnswersViewModel
  val ua                              = emptyUserAnswers

  "CheckYourAnswersViewModel" - {
    "getFinancialInstitutionSummaries must" - {
      val ua1 = emptyUserAnswers.withPage(NameOfFinancialInstitutionPage, "TestFinancialInstitutionName")
      val ua2 = ua1.withPage(HaveUniqueTaxpayerReferencePage, true)
      val ua3 = ua2.withPage(FirstContactEmailPage, "test@email.com")

      "only return rows for relevant populated answers" in {
        sut.getFinancialInstitutionSummaries(emptyUserAnswers).length mustBe 0
        sut.getFinancialInstitutionSummaries(ua1).length mustBe 1
        sut.getFinancialInstitutionSummaries(ua2).length mustBe 2
        sut.getFinancialInstitutionSummaries(ua3).length mustBe 2
      }
    }
    "getFirstContactSummaries must" - {
      "only return rows for relevant populated answers" in {
        val ans = ua
          .withPage(FirstContactPhoneNumberPage, "04025429852")
          .withPage(FirstContactEmailPage, "test@email.com")
        val ans2 = ua
          .withPage(FirstContactPhoneNumberPage, "04025429852")
          .withPage(SecondContactEmailPage, "test@email.com")

        sut.getFirstContactSummaries(ans).length mustBe 2
        sut.getFirstContactSummaries(ans2).length mustBe 1
      }
      "display phone number not provided row by default" in {
        val ans = ua
          .withPage(FirstContactNamePage, "MrTest")
          .withPage(FirstContactPhoneNumberPage, "04025429852")
        val ans2 = ua.withPage(FirstContactNamePage, "MrTest")

        sut.getFirstContactSummaries(ans).length mustBe 2
        sut.getFirstContactSummaries(ans2).length mustBe 2
      }
    }
    "getSecondContactSummaries must" - {
      "only return rows for relevant populated answers" in {
        val ans = ua.withPage(SecondContactNamePage, "SecondContact")

        sut.getSecondContactSummaries(emptyUserAnswers).length mustBe 0
        sut.getSecondContactSummaries(ans).length mustBe 1
      }
    }

    "getRegisteredBusinessSummaries must" - {
      "only return rows for relevant populated answers" in {
        val ans = ua
          .withPage(ReportForRegisteredBusinessPage, true)
          .withPage(HaveGIINPage, true)
          .withPage(WhatIsGIINPage, "someGIIN")
          .withPage(FetchedRegisteredAddressPage, AddressResponse("line1", Some("line2"), Some("line3"), Some("line4"), Some("ab12cd"), "GB"))

        sut.getRegisteredBusinessSummaries(emptyUserAnswers).length mustBe 0
        sut.getRegisteredBusinessSummaries(ans).length mustBe 4
      }
    }
  }

}
