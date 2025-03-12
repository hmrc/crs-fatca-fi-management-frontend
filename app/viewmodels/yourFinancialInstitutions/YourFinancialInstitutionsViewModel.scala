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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.common.accessibleActionItem
import viewmodels.govuk.all.{FluentActionItem, SummaryListRowViewModel}
import viewmodels.implicits._

object YourFinancialInstitutionsViewModel {

  def getValueContent(name: String, fiIsRegisteredBusiness: Boolean = false): Content =
    if (fiIsRegisteredBusiness)
      HtmlContent(s"""
         |<span class="govuk-!-margin-right-2" style="max-width: 180px">$name</span>
         |<strong class="govuk-tag">
         |  Registered business
         |</strong>""".stripMargin)
    else {
      HtmlContent(s"""<span class="govuk-!-margin-right-2">$name</span>""".stripMargin)
    }

  def getYourFinancialInstitutionsRows(institutions: Seq[FIDetail])(implicit messages: Messages): Seq[SummaryListRow] =
    institutions.map {
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
