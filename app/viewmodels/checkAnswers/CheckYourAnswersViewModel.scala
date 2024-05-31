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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryListRow}
import viewmodels.govuk.summarylist._

object CheckYourAnswersViewModel {

  def accessibleActionItem(messageKey: String, href: String)(implicit messages: Messages): ActionItem =
    ActionItemViewModel(
      content = HtmlContent(
        s"""
           |<span aria-hidden="true">${messages(messageKey)}</span>
           |""".stripMargin
      ),
      href = href
    )

  private def getAddressRow(ua: UserAnswers)(implicit messages: Messages) =
    AddressLookupSummary.row(ua)

  def getFinancialInstitutionSummaries(ua: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      NameOfFinancialInstitutionSummary.row(ua),
      HaveUniqueTaxpayerReferenceSummary.row(ua),
      WhatIsUniqueTaxpayerReferenceSummary.row(ua),
      SendReportsSummary.row(ua),
      HaveGIINSummary.row(ua),
      WhatIsGIINSummary.row(ua),
      getAddressRow(ua)
    ).flatten

  def getFirstContactSummaries(ua: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    ContactNameSummary.row(ua),
    FirstContactEmailSummary.row(ua),
    FirstContactPhoneNumberSummary.row(ua)
  ).flatten

  def getSecondContactSummaries(ua: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      SecondContactExistsSummary.row(ua),
      SecondContactNameSummary.row(ua),
      SecondContactEmailSummary.row(ua),
      SecondContactPhoneNumberSummary.row(ua)
    ).flatten

}
