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

package pages.addFinancialInstitution.IsRegisteredBusiness

import models.{AddressResponse, UserAnswers}
import pages.QuestionPage
import pages.addFinancialInstitution.{NonUkAddressPage, SelectedAddressLookupPage, UkAddressPage}
import play.api.libs.json.JsPath

import scala.util.Try

case object FetchedRegisteredAddressPage extends QuestionPage[AddressResponse] {

  override def path: JsPath = JsPath \ toString

  override def cleanup(value: Option[AddressResponse], userAnswers: UserAnswers): Try[UserAnswers] =
    value.fold(Try(userAnswers))(
      _ =>
        List(
          SelectedAddressLookupPage,
          UkAddressPage
        ).foldLeft(Try(userAnswers))(
          (ua: Try[UserAnswers], page: QuestionPage[_]) => ua.flatMap(_.remove(page))
        )
    )

}
