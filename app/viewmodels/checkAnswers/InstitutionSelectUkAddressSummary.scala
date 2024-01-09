package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.InstitutionSelectUkAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object InstitutionSelectUkAddressSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(InstitutionSelectUkAddressPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "institutionSelectUkAddress.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.InstitutionSelectUkAddressController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("institutionSelectUkAddress.change.hidden"))
          )
        )
    }
}
