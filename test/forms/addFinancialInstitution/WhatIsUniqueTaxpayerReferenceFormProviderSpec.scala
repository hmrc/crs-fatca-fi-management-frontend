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

class WhatIsUniqueTaxpayerReferenceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey              = "whatIsUniqueTaxpayerReference.error.required"
  val invalidFormatKey         = "whatIsUniqueTaxpayerReference.error.invalidFormat"
  val invalidKey               = "whatIsUniqueTaxpayerReference.error.invalid"
  val allowedLengths: Set[Int] = Set(10, 13)

  val form = new WhatIsUniqueTaxpayerReferenceFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validUtr
    )

    behave like fieldWithFixedLengthsNumeric(
      form,
      fieldName,
      allowedLengths,
      lengthError = FormError(fieldName, invalidFormatKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithNonEmptyWhitespace(
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

}
