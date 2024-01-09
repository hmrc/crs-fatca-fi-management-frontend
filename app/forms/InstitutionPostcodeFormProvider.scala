package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class InstitutionPostcodeFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("institutionPostcode.error.required")
        .verifying(maxLength(10, "institutionPostcode.error.length"))
    )
}
