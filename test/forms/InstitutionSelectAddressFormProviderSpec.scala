package forms

import forms.behaviours.OptionFieldBehaviours
import models.InstitutionSelectAddress
import play.api.data.FormError

class InstitutionSelectAddressFormProviderSpec extends OptionFieldBehaviours {

  val form = new InstitutionSelectAddressFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "institutionSelectAddress.error.required"

    behave like optionsField[InstitutionSelectAddress](
      form,
      fieldName,
      validValues  = InstitutionSelectAddress.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
