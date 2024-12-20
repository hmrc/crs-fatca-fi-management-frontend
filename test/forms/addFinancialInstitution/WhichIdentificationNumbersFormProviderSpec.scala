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

import forms.behaviours.CheckboxFieldBehaviours
import models.FinancialInstitutions.TINType
import play.api.data.FormError

class WhichIdentificationNumbersFormProviderSpec extends CheckboxFieldBehaviours {

  val formProvider = new WhichIdentificationNumbersFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "whichIdentificationNumbers.error.required"

    behave like checkboxField[TINType](
      formProvider,
      fieldName,
      validValues = TINType.whichIdValues,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      formProvider,
      fieldName,
      requiredKey
    )
  }

  "bind when UTR and CRN are selected" in {
    val form = formProvider.bind(Map("value[0]" -> "UTR", "value[1]" -> "CRN"))
    form.errors mustBe empty
  }

  "fail to bind when URN is selected with UTR" in {
    val form = formProvider.bind(Map("value[0]" -> "UTR", "value[2]" -> "URN"))
    form.errors must contain(FormError("value[2]", "error.invalid"))
  }

  "fail to bind when URN is selected with CRN" in {
    val form = formProvider.bind(Map("value[0]" -> "CRN", "value[2]" -> "URN"))
    form.errors must contain(FormError("value[2]", "error.invalid"))
  }

}
