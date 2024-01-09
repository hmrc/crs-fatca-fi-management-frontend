package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.InstitutionSelectAddress

class InstitutionSelectAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[InstitutionSelectAddress] =
    Form(
      "value" -> enumerable[InstitutionSelectAddress]("institutionSelectAddress.error.required")
    )
}
