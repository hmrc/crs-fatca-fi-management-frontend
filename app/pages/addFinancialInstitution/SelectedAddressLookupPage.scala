/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{AddressLookup, UserAnswers}
import pages.QuestionPage
import pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage
import play.api.libs.json.JsPath

import scala.util.Try

object SelectedAddressLookupPage extends QuestionPage[AddressLookup] {
  override def path: JsPath = JsPath \ toString

  override def toString: String = "selectedAddressLookup"

  override def cleanup(value: Option[AddressLookup], userAnswers: UserAnswers): Try[UserAnswers] =
    value.fold(Try(userAnswers))(
      _ =>
        List(
          UkAddressPage,
          FetchedRegisteredAddressPage
        ).foldLeft(Try(userAnswers))(
          (ua: Try[UserAnswers], page: QuestionPage[_]) => ua.flatMap(_.remove(page))
        )
    )

}
