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
import play.api.data.FormError

class FirstContactPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "firstContactPhoneNumber.error.required"
  val lengthKey   = "firstContactPhoneNumber.error.length"
  val invalidKey  = "firstContactPhoneNumber.error.invalid"
  val maxLength   = 24
  val form        = new FirstContactPhoneNumberFormProvider()()

  ".value" - {
    val invalidStrings = Seq("not a phone number", "*44 7802342345", "44-7802342345", "44#7802342345", "44+7802342345")
    val fieldName      = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPhoneNumber(maxLength)
    )

    behave like fieldWithMaxLengthPhoneNumber(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq())
    )

    for (invalidString <- invalidStrings)
      behave like fieldWithInvalidData(
        form,
        fieldName,
        invalidString,
        error = FormError(fieldName, invalidKey),
        Some(invalidString)
      )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
