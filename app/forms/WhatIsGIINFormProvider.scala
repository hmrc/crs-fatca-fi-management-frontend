package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class WhatIsGIINFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("whatIsGIIN.error.required")
        .verifying(maxLength(100, "whatIsGIIN.error.length"))
    )
}
