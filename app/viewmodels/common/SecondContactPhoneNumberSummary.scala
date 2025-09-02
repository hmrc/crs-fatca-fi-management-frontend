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

package viewmodels.common

import models.{AnswersReviewPageType, CheckMode, UserAnswers}
import pages.addFinancialInstitution.SecondContactPhoneNumberPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SecondContactPhoneNumberSummary {

  def row(ua: UserAnswers, pageType: AnswersReviewPageType)(implicit messages: Messages): Option[SummaryListRow] =
    ua.get(SecondContactPhoneNumberPage) match {
      case Some(answer) => Some(createRow(answer, pageType))
      case _            => Some(createRow(messages("site.notProvided"), pageType))
    }

  private def createRow(answer: String, pageType: AnswersReviewPageType)(implicit messages: Messages) =
    SummaryListRowViewModel(
      key = s"secondContactPhoneNumber.${pageType.labelPrefix}YourAnswersLabel",
      value = ValueViewModel(HtmlContent(answer)),
      actions = Seq(
        accessibleActionItem("site.change", controllers.addFinancialInstitution.routes.SecondContactPhoneNumberController.onPageLoad(CheckMode).url)
          .withVisuallyHiddenText(messages("secondContactPhoneNumber.change.hidden"))
      )
    )

}
