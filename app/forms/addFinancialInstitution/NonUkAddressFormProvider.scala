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

package forms.addFinancialInstitution

import javax.inject.Inject
import forms.mappings.Mappings
import models.{Address, Country}
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.RegexConstants

class NonUkAddressFormProvider @Inject() extends Mappings with RegexConstants {

  val addressLineLength = 35

  def apply(countryList: Seq[Country]): Form[Address] = Form(
    mapping(
      "addressLine1" -> validatedText(
        "nonUkAddress.error.addressLine1.required",
        "nonUkAddress.error.addressLine1.invalid",
        "nonUkAddress.error.addressLine1.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine2" -> validatedOptionalText(
        "nonUkAddress.error.addressLine2.invalid",
        "nonUkAddress.error.addressLine2.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine3" -> validatedOptionalText(
        "nonUkAddress.error.addressLine3.invalid",
        "nonUkAddress.error.addressLine3.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine4" -> validatedOptionalText(
        "nonUkAddress.error.addressLine4.invalid",
        "nonUkAddress.error.addressLine4.length",
        apiAddressRegex,
        addressLineLength
      ),
      "postCode" -> optionalPostcode(
        "nonUkAddress.error.postcode.length"
      ),
      "country" -> text("nonUkAddress.error.country.required")
        .verifying("nonUkAddress.error.country.required", value => countryList.exists(_.code == value))
        .transform[Country](
          value => countryList.find(_.code == value).getOrElse(throw new IllegalStateException(s"Country with code [$value] not found")),
          _.code
        )
    )(Address.apply)(Address.unapply)
  )

}
