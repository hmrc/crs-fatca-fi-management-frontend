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

package forms.mappings

import models.Enumerable
import play.api.data.Forms.of
import play.api.data.{FieldMapping, Mapping}

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def validatedText(
    requiredKey: String,
    invalidKey: String,
    lengthKey: String,
    regex: String,
    maxLength: Int,
    minLength: Int = 1,
    msgArg: String = ""
  ): FieldMapping[String] =
    of(validatedTextFormatter(requiredKey, invalidKey, lengthKey, regex, maxLength, minLength, msgArg))

  protected def validatedOptionalText(invalidKey: String, lengthKey: String, regex: String, length: Int): FieldMapping[Option[String]] =
    of(validatedOptionalTextFormatter(invalidKey, lengthKey, regex, length))

  protected def int(
    requiredKey: String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey: String = "error.nonNumeric",
    args: Seq[String] = Seq.empty
  ): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def boolean(
    requiredKey: String = "error.required",
    invalidKey: String = "error.boolean",
    args: Seq[String] = Seq.empty
  ): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A](
    requiredKey: String = "error.required",
    invalidKey: String = "error.invalid",
    args: Seq[String] = Seq.empty
  )(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def localDate(
    invalidKey: String,
    allRequiredKey: String,
    twoRequiredKey: String,
    requiredKey: String,
    args: Seq[String] = Seq.empty
  ): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, allRequiredKey, twoRequiredKey, requiredKey, args))

  protected def validatedUTR(requiredKey: String, invalidKey: String, invalidFormatKey: String, regex: String, msgArg: String = ""): FieldMapping[String] =
    of(validatedIdFormatter(requiredKey, invalidKey, invalidFormatKey, regex, msgArg, acceptedLengths = Seq(10, 13), isUtr = true))

  protected def validatedCRN(requiredKey: String, invalidKey: String, invalidFormatKey: String, regex: String, msgArg: String = ""): FieldMapping[String] =
    of(validatedIdFormatter(requiredKey, invalidKey, invalidFormatKey, regex, msgArg, acceptedLengths = Seq(8)))

  protected def validatedURN(requiredKey: String, invalidKey: String, invalidFormatKey: String, msgArg: String = ""): FieldMapping[String] =
    of(validatedUrnFormatter(requiredKey, invalidKey, invalidFormatKey, msgArg))

  protected def mandatoryPostcode(requiredKey: String,
                                  lengthKey: String,
                                  invalidKey: String,
                                  regex: String,
                                  invalidCharKey: String,
                                  InvalidCharRegex: String
  ): Mapping[String] =
    of(mandatoryPostcodeFormatter(requiredKey, lengthKey, invalidKey, regex, invalidCharKey, InvalidCharRegex))

  protected def optionalPostcode(lengthKey: String): FieldMapping[Option[String]] =
    of(optionalPostcodeFormatter(lengthKey))

  protected def mandatoryGIIN(requiredKey: String, lengthKey: String, invalidKey: String, formatKey: String, invalidCharKey: String): FieldMapping[String] =
    of(mandatoryGIINFormatter(requiredKey, lengthKey, invalidKey, formatKey, invalidCharKey))

}
