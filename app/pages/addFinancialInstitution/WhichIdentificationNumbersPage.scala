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

package pages.addFinancialInstitution

import models.{UserAnswers, WhichIdentificationNumbers}
import pages.{CompanyRegistrationNumberPage, QuestionPage, TrustURNPage}
import play.api.libs.json.JsPath

import scala.util.Try

case object WhichIdentificationNumbersPage extends QuestionPage[Set[WhichIdentificationNumbers]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "whichIdentificationNumbers"

  def cleanUpUnselectedTINPages(selectedTINs: Set[WhichIdentificationNumbers], userAnswers: UserAnswers): Try[UserAnswers] = {
    val tinPages = Seq(
      WhichIdentificationNumbers.UTR -> WhatIsUniqueTaxpayerReferencePage,
      WhichIdentificationNumbers.CRN -> CompanyRegistrationNumberPage,
      WhichIdentificationNumbers.TRN -> TrustURNPage
    ).collect {
      case (tin, page) if !selectedTINs.contains(tin) => page
    }

    removePages(tinPages, userAnswers)
  }

}
