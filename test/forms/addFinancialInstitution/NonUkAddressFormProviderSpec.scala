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

import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Gen
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class NonUkAddressFormProviderSpec extends StringFieldBehaviours {

  private val countries = Seq(
    Country("valid", "AD", "Andorra"),
    Country("valid", "FJ", "Fiji"),
    Country("valid", "GG", "Guernsey"),
    Country("valid", "GG", "Guernsey"),
    Country("valid", "GG", "Guernsey")
  )

  val form = new NonUkAddressFormProvider()(countries)

  val addressLineMaxLength = 35

  ".addressLine1" - {

    val fieldName   = "addressLine1"
    val requiredKey = "nonUkAddress.error.addressLine1.required"
    val invalidKey  = "nonUkAddress.error.addressLine1.invalid"
    val lengthKey   = "nonUkAddress.error.addressLine1.length"

    behave like fieldThatBindsValidDataWithoutInvalidError(
      form,
      fieldName,
      RegexpGen.from(apiAddressRegex),
      invalidKey
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = addressLineMaxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }

  ".addressLine2" - {

    val fieldName  = "addressLine2"
    val invalidKey = "nonUkAddress.error.addressLine2.invalid"
    val lengthKey  = "nonUkAddress.error.addressLine2.length"

    behave like fieldThatBindsValidDataWithoutInvalidError(
      form,
      fieldName,
      RegexpGen.from(apiAddressRegex),
      invalidKey
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = addressLineMaxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }

  ".addressLine3" - {

    val fieldName   = "addressLine3"
    val requiredKey = "nonUkAddress.error.addressLine3.required"
    val invalidKey  = "nonUkAddress.error.addressLine3.invalid"
    val lengthKey   = "nonUkAddress.error.addressLine3.length"

    behave like fieldThatBindsValidDataWithoutInvalidError(
      form,
      fieldName,
      RegexpGen.from(apiAddressRegex),
      invalidKey
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = addressLineMaxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }

  ".addressLine4" - {

    val fieldName  = "addressLine4"
    val invalidKey = "nonUkAddress.error.addressLine4.invalid"
    val lengthKey  = "nonUkAddress.error.addressLine4.length"

    behave like fieldThatBindsValidDataWithoutInvalidError(
      form,
      fieldName,
      RegexpGen.from(apiAddressRegex),
      invalidKey
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = addressLineMaxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }

  ".postCode" - {

    val fieldName = "postCode"
    val lengthKey = "nonUkAddress.error.postcode.length"

    val postCodeMaxLength = 10

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPostCodes
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = postCodeMaxLength,
      lengthError = FormError(fieldName, lengthKey)
    )
  }

  ".country" - {

    val fieldName   = "country"
    val requiredKey = "nonUkAddress.error.country.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(countries.map(_.code))
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      invalidCountry,
      error = FormError(fieldName, requiredKey)
    )
  }

}
