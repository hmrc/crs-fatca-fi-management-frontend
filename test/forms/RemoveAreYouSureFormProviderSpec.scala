package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveAreYouSureFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "removeAreYouSure.error.required"
  val invalidKey = "error.boolean"

  val form = new RemoveAreYouSureFormProvider()()

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
