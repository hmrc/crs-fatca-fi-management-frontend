package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class InstitutionPostcodeFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "institutionPostcode.error.required"
  val lengthKey = "institutionPostcode.error.length"
  val maxLength = 10

  val form = new InstitutionPostcodeFormProvider()()

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
