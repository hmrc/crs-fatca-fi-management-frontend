package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class WhatIsGIINFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "whatIsGIIN.error.required"
  val lengthKey = "whatIsGIIN.error.length"
  val maxLength = 100

  val form = new WhatIsGIINFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
