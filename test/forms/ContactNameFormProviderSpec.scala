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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import utils.RegexConstants
import wolfendale.scalacheck.regexp.RegexpGen

class ContactNameFormProviderSpec extends StringFieldBehaviours with RegexConstants {

  val requiredKey = "contactName.error.required"
  val lengthKey   = "contactName.error.length"
  val invalidKey  = "contactName.error.invalid"
  val maxLength   = 35

  val form = new ContactNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidDataWithoutInvalidError(
      form,
      fieldName,
      RegexpGen.from(orgNameRegex),
      invalidKey
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
