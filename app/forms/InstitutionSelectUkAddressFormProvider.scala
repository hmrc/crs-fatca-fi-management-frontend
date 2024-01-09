package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class InstitutionSelectUkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("institutionSelectUkAddress.error.required")
        .verifying(maxLength(100, "institutionSelectUkAddress.error.length"))
    )
}
