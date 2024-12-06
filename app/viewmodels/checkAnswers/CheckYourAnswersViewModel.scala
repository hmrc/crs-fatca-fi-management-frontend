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

import models.{CheckAnswers, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.common._

object CheckYourAnswersViewModel {

  def getFinancialInstitutionSummaries(ua: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      ReportForRegisteredBusinessSummary.row(ua, CheckAnswers),
      NameOfFinancialInstitutionSummary.row(ua, CheckAnswers),
      getIdRows(ua, CheckAnswers),
      getGIINRows(ua, CheckAnswers),
      getAddressRow(ua, CheckAnswers)
    ).flatten

  def getRegisteredBusinessSummaries(ua: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      ReportForRegisteredBusinessSummary.row(ua, CheckAnswers),
      IsThisYourBusinessNameSummary.row(ua, CheckAnswers),
      getGIINRows(ua, CheckAnswers),
      getAddressRow(ua, CheckAnswers)
    ).flatten

}
