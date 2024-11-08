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

import forms.mappings.Mappings
import models.{Address, Country}
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.RegexConstants

class UkAddressFormProvider extends Mappings with RegexConstants {

  val addressLineLength = 35

  def apply(): Form[Address] = Form(
    mapping(
      "addressLine1" -> validatedText(
        "ukAddress.error.addressLine1.required",
        "ukAddress.error.addressLine1.invalid",
        "ukAddress.error.addressLine1.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine2" -> validatedOptionalText("ukAddress.error.addressLine2.invalid",
                                              "ukAddress.error.addressLine2.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "addressLine3" -> validatedText(
        "ukAddress.error.addressLine3.required",
        "ukAddress.error.addressLine3.invalid",
        "ukAddress.error.addressLine3.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine4" -> validatedOptionalText("ukAddress.error.addressLine4.invalid",
                                              "ukAddress.error.addressLine4.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "postCode" -> mandatoryPostcode(
        "ukAddress.error.postcode.required",
        "ukAddress.error.postcode.length",
        "ukAddress.error.postcode.invalid",
        regexPostcode,
        "ukAddress.error.postcode.chars",
        postCodeAllowedChars
      ).verifying("ukAddress.error.postcode.nonUk", postcode => !Seq("GY", "JE", "IM").exists(postcode.startsWith))
        .transform[Option[String]](
          postCode => Option(postCode),
          _.getOrElse(throw new IllegalStateException("postCode is empty"))
        )
    )(
      (addressLine1, addressLine2, addressLine3, addressLine4, postCode) =>
        Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode, Country.GB)
    )(
      address => Some((address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4, address.postCode))
    )
  )

}
