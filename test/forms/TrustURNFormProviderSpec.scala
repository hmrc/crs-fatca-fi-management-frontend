package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class TrustURNFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "trustURN.error.required"
  val lengthKey   = "trustURN.error.length"
  val maxLength   = 20

  val form = new TrustURNFormProvider()()

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
