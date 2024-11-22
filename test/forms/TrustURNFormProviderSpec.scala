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

class TrustURNFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "trustURN.error.required"
  val charKey     = "trustURN.error.invalidChar"
  val formatKey   = "trustURN.error.invalidFormat"
  val maxLength   = 15

  val form = new TrustURNFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validUrn
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "ABCDEF1234567",
      FormError(fieldName, formatKey)
    )

    behave like fieldWithInvalidFormat(
      form,
      fieldName,
      "ABCDEF!1234567!",
      FormError(fieldName, charKey)
    )
  }

}
