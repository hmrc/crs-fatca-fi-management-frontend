/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.behaviours

import play.api.data.{Form, FormError}
import uk.gov.hmrc.emailaddress.EmailAddress

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(lengthError)
      }
    }

  def fieldWithMaxLengthPhoneNumber(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(validPhoneNumberTooLong(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithNonEmptyWhitespace(form: Form[_], fieldName: String, requiredError: FormError): Unit =
    s"must not bind strings of only whitespace" in {

      val result = form.bind(Map(fieldName -> " ")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

  def emailAddressField(form: Form[_], fieldName: String, maxLength: Int, invalidError: FormError): Unit =
    s"not bind invalid email address" in {
      forAll(stringsWithMaxLength(maxLength)) {
        email =>
          whenever(!EmailAddress.isValid(email)) {
            val result = form.bind(Map(fieldName -> email)).apply(fieldName)
            result.errors mustEqual Seq(invalidError)
          }
      }
    }

  def fieldWithFixedLengthNumeric(form: Form[_], fieldName: String, length: Int, lengthError: FormError): Unit =
    s"must not bind strings that are not $length characters" in {

      forAll(stringsNotOfFixedLengthNumeric(length) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(lengthError)
      }
    }

}
