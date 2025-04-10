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

import models.{AnswersReviewPageType, CheckMode, UserAnswers}
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.common.accessibleActionItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ReportForRegisteredBusinessSummary {

  def row(answers: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ReportForRegisteredBusinessPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = s"reportForRegisteredBusiness.${pageType.labelPrefix}YourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            accessibleActionItem("site.change",
                                 controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(CheckMode).url
            )
              .withVisuallyHiddenText(messages(s"reportForRegisteredBusiness.${pageType.labelPrefix}.hidden"))
          )
        )
    }

}
