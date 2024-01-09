package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class IsThisInstitutionAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("isThisInstitutionAddress.error.required")
    )
}
