package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.InstitutionSelectNonUkAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object InstitutionSelectNonUkAddressSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(InstitutionSelectNonUkAddressPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "institutionSelectNonUkAddress.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.InstitutionSelectNonUkAddressController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("institutionSelectNonUkAddress.change.hidden"))
          )
        )
    }
}
