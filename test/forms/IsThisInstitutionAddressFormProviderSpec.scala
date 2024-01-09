package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class IsThisInstitutionAddressFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "isThisInstitutionAddress.error.required"
  val invalidKey = "error.boolean"

  val form = new IsThisInstitutionAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
