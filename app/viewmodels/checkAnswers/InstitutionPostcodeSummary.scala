package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.InstitutionPostcodePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object InstitutionPostcodeSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(InstitutionPostcodePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "institutionPostcode.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.InstitutionPostcodeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("institutionPostcode.change.hidden"))
          )
        )
    }
}
