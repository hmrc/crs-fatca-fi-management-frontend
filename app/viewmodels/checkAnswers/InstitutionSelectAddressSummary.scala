package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.InstitutionSelectAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object InstitutionSelectAddressSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(InstitutionSelectAddressPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"institutionSelectAddress.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = "institutionSelectAddress.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.InstitutionSelectAddressController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("institutionSelectAddress.change.hidden"))
          )
        )
    }
}