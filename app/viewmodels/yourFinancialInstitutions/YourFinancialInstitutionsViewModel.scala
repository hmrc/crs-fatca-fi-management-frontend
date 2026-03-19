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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.listwithactions.{ListWithActions, ListWithActionsAction, ListWithActionsItem}

object YourFinancialInstitutionsViewModel {

  def getYourFinancialInstitutionsRows(institutions: Seq[FIDetail])(implicit messages: Messages): ListWithActions = {
    val orderedInstitutions = orderInstitutions(institutions)
    val items = orderedInstitutions.map {
      institution =>
        ListWithActionsItem(
          name = getValueContent(institution.FIName, institution.IsFIUser),
          actions = Seq(
            ListWithActionsAction(
              if (institution.IsFIUser) {
                controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(institution.FIID).url
              } else {
                controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(institution.FIID).url
              },
              Text(messages("site.change")),
              Some(messages("yourFinancialInstitutions.change.hidden", institution.FIName))
            ),
            ListWithActionsAction(
              controllers.routes.UserAccessController.onPageLoad(institution.FIID).url,
              Text(messages("site.remove")),
              Some(messages("yourFinancialInstitutions.remove.hidden", institution.FIName))
            ),
            ListWithActionsAction(
              controllers.routes.YourFinancialInstitutionsController.onPageLoad().url,
              Text(messages("yourFinancialInstitutions.link.manageReports")),
              Some(messages("yourFinancialInstitutions.manageReports.hidden", institution.FIName))
            )
          )
        )
    }
    ListWithActions(items = items)
  }

  private def getValueContent(name: String, fiIsRegisteredBusiness: Boolean): HtmlContent = {
    val registeredBusinessTag =
      if (fiIsRegisteredBusiness) {
        """<strong class="govuk-tag" style="max-width: 180px !important;">Registered business</strong>"""
      } else {
        ""
      }

    HtmlContent(s"""
         |<span class="govuk-!-margin-right-2" style="max-width: 180px">$name</span>
         |$registeredBusinessTag
  """.stripMargin.trim)
  }

  private def orderInstitutions(institutions: Seq[FIDetail]): Seq[FIDetail] =
    institutions.sortBy(
      fi => (!fi.IsFIUser, fi.FIName.toUpperCase)
    )

}
