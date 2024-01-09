package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class InstitutionSelectNonUkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("institutionSelectNonUkAddress.error.required")
        .verifying(maxLength(100, "institutionSelectNonUkAddress.error.length"))
    )
}
