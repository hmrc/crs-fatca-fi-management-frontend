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

package viewmodels.yourFinancialInstitutions

import models.FinancialInstitutions.FIDetail
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.common.accessibleActionItem
import viewmodels.govuk.all.{FluentActionItem, SummaryListRowViewModel}
import viewmodels.implicits._

object YourFinancialInstitutionsViewModel {

  def getYourFinancialInstitutionsRows(institutions: Seq[FIDetail])(implicit messages: Messages): Seq[SummaryListRow] = {
    val orderedInstitutions = orderInstitutions(institutions)
    orderedInstitutions.map {
      institution =>
        SummaryListRowViewModel(
          key = Key("", "govuk-!-display-none"),
          value = Value(getValueContent(institution.FIName, institution.IsFIUser)),
          actions = Seq(
            accessibleActionItem(
              "site.change",
              if (institution.IsFIUser) {
                controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(institution.FIID).url
              } else {
                controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(institution.FIID).url
              }
            )
              .withVisuallyHiddenText(messages("yourFinancialInstitutions.change.hidden", institution.FIName)),
            accessibleActionItem("site.remove", controllers.routes.UserAccessController.onPageLoad(institution.FIID).url)
              .withVisuallyHiddenText(messages("yourFinancialInstitutions.remove.hidden", institution.FIName)),
            accessibleActionItem("yourFinancialInstitutions.link.manageReports", controllers.routes.YourFinancialInstitutionsController.onPageLoad().url)
              .withVisuallyHiddenText(messages("yourFinancialInstitutions.manageReports.hidden", institution.FIName))
          )
        )
    }
  }

  private def getValueContent(name: String, fiIsRegisteredBusiness: Boolean = false): HtmlContent = {
    val registeredBusinessTag =
      if (fiIsRegisteredBusiness)
        """<strong class="govuk-tag" style="max-width: 180px !important;">Registered business</strong>"""
      else ""

    HtmlContent(s"""
         |<span class="govuk-!-margin-right-2" style="max-width: 180px">$name</span>
         |$registeredBusinessTag
  """.stripMargin.trim)
  }

  private def orderInstitutions(institutions: Seq[FIDetail]): Seq[FIDetail] =
    institutions.sortBy(
      fi => (!fi.IsFIUser, fi.FIName)
    )

}
