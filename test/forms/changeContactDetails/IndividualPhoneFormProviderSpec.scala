package forms.changeContactDetails

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class IndividualPhoneFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "individualPhone.error.required"
  val lengthKey   = "individualPhone.error.length"
  val maxLength   = 24

  val form = new IndividualPhoneFormProvider()()

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
