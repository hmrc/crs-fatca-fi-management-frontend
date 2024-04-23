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

class WhatIsGIINFormProviderSpec extends StringFieldBehaviours {

  val form = new WhatIsGIINFormProvider()()

  ".value" - {

    val fieldName      = "value"
    val requiredKey    = "whatIsGIIN.error.required"
    val lengthKey      = "whatIsGIIN.error.length"
    val invalidKey     = "whatIsGIIN.error.invalid"
    val formatKey      = "whatIsGIIN.error.format"
    val invalidCharKey = "whatIsGIIN.error.char"

    val giinSetLength = 19

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validGIIN
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = giinSetLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithMinLengthAlpha(
      form,
      fieldName,
      minLength = giinSetLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "98O96B.00000.LE.350",
      FormError(fieldName, invalidKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "98O96B9.0000.LE.350",
      FormError(fieldName, formatKey),
      Some("format")
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "######.#####.##.###",
      FormError(fieldName, invalidCharKey),
      Some("chars")
    )
  }

}
