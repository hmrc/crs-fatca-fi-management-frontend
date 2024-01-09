package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class InstitutionSelectUkAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "institutionSelectUkAddress.error.required"
  val lengthKey = "institutionSelectUkAddress.error.length"
  val maxLength = 100

  val form = new InstitutionSelectUkAddressFormProvider()()

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
