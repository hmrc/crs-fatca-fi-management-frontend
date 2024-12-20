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

package viewmodels

import models.FinancialInstitutions.TINType
import models.FinancialInstitutions.TINType._
import models.{AnswersReviewPageType, CheckMode, UserAnswers}
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.addFinancialInstitution.{HaveGIINPage, WhichIdentificationNumbersPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryListRow}
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

package object common {

  def getAddressChangeRoute(answers: UserAnswers): String =
    answers
      .get(ReportForRegisteredBusinessPage) match {
      case Some(true) => controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(CheckMode).url
      case _          => controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(CheckMode).url
    }

  def accessibleActionItem(messageKey: String, href: String)(implicit messages: Messages): ActionItem =
    ActionItemViewModel(
      content = HtmlContent(
        s"""
           |<span aria-hidden="true">${messages(messageKey)}</span>
           |""".stripMargin
      ),
      href = href
    )

  def getFirstContactSummaries(ua: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    FirstContactNameSummary.row(ua, pageType),
    FirstContactEmailSummary.row(ua, pageType),
    FirstContactPhoneNumberSummary.row(ua, pageType)
  ).flatten

  def getSecondContactSummaries(ua: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      SecondContactExistsSummary.row(ua, pageType),
      SecondContactNameSummary.row(ua, pageType),
      SecondContactEmailSummary.row(ua, pageType),
      SecondContactPhoneNumberSummary.row(ua, pageType)
    ).flatten

  def getGIINRows(ua: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Seq[SummaryListRow] = {
    val haveGIIN = ua.get(HaveGIINPage)

    haveGIIN match {
      case None        => Seq(WhatIsGIINSummary.row(ua, pageType)).flatten
      case Some(true)  => Seq(HaveGIINSummary.row(ua, pageType), WhatIsGIINSummary.row(ua, pageType)).flatten
      case Some(false) => Seq(HaveGIINSummary.row(ua, pageType)).flatten
      case _           => Seq.empty
    }
  }

  def getIdRows(ua: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Seq[SummaryListRow] = {
    val idsUsed: Seq[TINType] = ua.get(WhichIdentificationNumbersPage).fold(Seq.empty[TINType])(_.toSeq)
    idsUsed match {
      case Seq(TINType.UTR) => Seq(WhichIdentificationNumbersSummary.row(ua), WhatIsUniqueTaxpayerReferenceSummary.row(ua, pageType)).flatten
      case Seq(TINType.CRN) => Seq(WhichIdentificationNumbersSummary.row(ua), CompanyRegistrationNumberSummary.row(ua)).flatten
      case Seq(TINType.UTR, TINType.CRN) =>
        Seq(WhichIdentificationNumbersSummary.row(ua), WhatIsUniqueTaxpayerReferenceSummary.row(ua, pageType), CompanyRegistrationNumberSummary.row(ua)).flatten
      case Seq(TINType.TRN) => Seq(WhichIdentificationNumbersSummary.row(ua), TrustURNSummary.row(ua)).flatten
      case _                => Seq.empty[SummaryListRow]
    }
  }

  def getAddressRow(ua: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Option[SummaryListRow] = {
    val addressLookup  = SelectedAddressLookupSummary.row(ua)
    val nonUkAddress   = NonUkAddressSummary.row(ua)
    val ukAddress      = UkAddressSummary.row(ua)
    val fetchedAddress = FetchedRegisteredAddressSummary.row(ua, pageType)

    (addressLookup.isDefined, nonUkAddress.isDefined, ukAddress.isDefined, fetchedAddress.isDefined) match {
      case (false, false, true, _)     => ukAddress
      case (false, true, false, false) => nonUkAddress
      case (true, false, false, _)     => addressLookup
      case (_, _, _, true)             => fetchedAddress
      case (_, _, _, false)            => None
    }

  }

}
