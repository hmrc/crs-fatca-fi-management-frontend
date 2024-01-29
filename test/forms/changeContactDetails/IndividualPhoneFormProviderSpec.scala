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

package forms.changeContactDetails

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class IndividualPhoneFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "individualPhone.error.required"
  val lengthKey   = "individualPhone.error.length"
  val formatKey   = "individualPhone.error.invalid"
  val maxLength   = 24

  val form = new IndividualPhoneFormProvider()()

  ".value" - {

    val fieldName = "value"

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

    behave like fieldWithInvalidData(
      form,
      fieldName,
      invalidString = "not a phone number",
      error = FormError(fieldName, formatKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}