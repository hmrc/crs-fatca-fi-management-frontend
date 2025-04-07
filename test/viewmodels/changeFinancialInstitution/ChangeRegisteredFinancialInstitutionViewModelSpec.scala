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

package viewmodels.changeFinancialInstitution

import base.SpecBase
import models.FinancialInstitutions.TINType
import models.FinancialInstitutions.TINType._
import models.{AddressResponse, CheckAnswers, CheckMode, CompanyRegistrationNumber, GIINumber, TrustUniqueReferenceNumber, UniqueTaxpayerReference, UserAnswers}
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.{CompanyRegistrationNumberPage, TrustURNPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.common.{getAddressChangeRoute, getFirstContactSummaries, getSecondContactSummaries}

class ChangeRegisteredFinancialInstitutionViewModelSpec extends SpecBase {

  implicit val mockMessages: Messages                         = stubMessages()
  val sut: ChangeRegisteredFinancialInstitutionViewModel.type = ChangeRegisteredFinancialInstitutionViewModel
  val ua: UserAnswers                                         = emptyUserAnswers

  "ChangeRegisteredFinancialInstitutionViewModel" - {
    "getChangeRegisteredFinancialInstitutionSummaries must" - {
      val uaWithoutGIIN = ua
        .withPage(ReportForRegisteredBusinessPage, true)
        .withPage(NameOfFinancialInstitutionPage, "Test")
        .withPage(HaveGIINPage, false)
        .withPage(FetchedRegisteredAddressPage, AddressResponse("line1", Some("line2"), Some("line3"), Some("line4"), Some("ab12cd"), "GB"))

      val uaWithGIIN = ua
        .withPage(ReportForRegisteredBusinessPage, true)
        .withPage(NameOfFinancialInstitutionPage, "Test")
        .withPage(HaveGIINPage, true)
        .withPage(WhatIsGIINPage, GIINumber("someGIIN"))
        .withPage(FetchedRegisteredAddressPage, AddressResponse("line1", Some("line2"), Some("line3"), Some("line4"), Some("ab12cd"), "GB"))

      "only return rows for relevant populated answers" in {
        sut.getChangeRegisteredFinancialInstitutionSummaries("123451231", uaWithoutGIIN).length mustBe 5
        sut.getChangeRegisteredFinancialInstitutionSummaries("123451231", uaWithGIIN).length mustBe 6
      }
    }
  }

}
