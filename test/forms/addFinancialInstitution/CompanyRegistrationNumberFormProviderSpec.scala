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

class CompanyRegistrationNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey           = "whatIsCompanyRegistrationNumber.error.required"
  val invalidFormatKey      = "whatIsCompanyRegistrationNumber.error.invalidFormat"
  val invalidKey            = "whatIsCompanyRegistrationNumber.error.invalid"
  val fixedLength: Set[Int] = Set(8)

  val form = new CompanyRegistrationNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validCrn
    )

    behave like fieldWithFixedLengthsNumeric(
      form,
      fieldName,
      fixedLength,
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
