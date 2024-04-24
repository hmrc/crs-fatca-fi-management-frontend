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

class FirstContactEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "firstContactEmail.error.required"
  val lengthKey   = "firstContactEmail.error.length"
  val formatError = "firstContactEmail.error.format"
  val maxLength   = 132

  val form = new FirstContactEmailFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like emailAddressField(
      form,
      fieldName,
      maxLength,
      invalidError = FormError(fieldName, formatError)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
