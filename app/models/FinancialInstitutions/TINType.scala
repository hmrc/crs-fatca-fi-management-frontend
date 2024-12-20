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

package models.FinancialInstitutions

import models.Enumerable
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait TINType

object TINType extends Enumerable.Implicits {

  case object UTR extends TINType
  case object GIIN extends TINType
  case object CRN extends TINType
  case object TRN extends TINType

  val allValues: IndexedSeq[TINType]     = IndexedSeq(UTR, CRN, TRN, GIIN)
  val whichIdValues: IndexedSeq[TINType] = IndexedSeq(UTR, CRN, TRN)

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] = {
    val items = whichIdValues.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"whichIdentificationNumbers.${value.toString}")),
          fieldId = "value",
          index = index,
          value = value.toString
        )
    }
    items.patch(2, Seq(CheckboxItem(divider = Some("or"))), 0)

  }

  implicit val enumerable: Enumerable[TINType] =
    Enumerable(
      allValues.map(
        v => v.toString -> v
      ): _*
    )

}
