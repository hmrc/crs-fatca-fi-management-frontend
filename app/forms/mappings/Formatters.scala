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
import play.api.data.FormError
import play.api.data.format.Formatter
import utils.RegexConstants

import scala.util.control.Exception.nonFatalCatch

trait Formatters extends Transforms with RegexConstants {

  private def removeNonBreakingSpaces(str: String) =
    str.replaceAll("\u00A0", " ")

  private[mappings] def stringFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] =
    new Formatter[String] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case None                      => Left(Seq(FormError(key, errorKey, args)))
          case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
          case Some(s)                   => Right(s)
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)

    }

  private[mappings] def booleanFormatter(
    requiredKey: String,
    invalidKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(
    requiredKey: String,
    wholeNumberKey: String,
    nonNumericKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s =>
              nonFatalCatch
                .either(s.toInt)
                .left
                .map(
                  _ => Seq(FormError(key, nonNumericKey, args))
                )
          }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)

    }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty)(implicit
    ev: Enumerable[A]
  ): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap {
          str =>
            ev.withName(str) match {
              case Some(value) => Right(value)
              case None        => Left(Seq(FormError(key, invalidKey, args)))
            }
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)

    }

  protected def validatedTextFormatter(
    requiredKey: String,
    invalidKey: String,
    lengthKey: String,
    regex: String,
    maxLength: Int,
    minLength: Int = 1,
    msgArg: String = ""
  ): Formatter[String] =
    new Formatter[String] {
      private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey, msgArg)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        dataFormatter
          .bind(key, data)
          .flatMap {
            case str if !str.matches(regex)    => Left(Seq(FormError(key, invalidKey)))
            case str if str.length > maxLength => Left(Seq(FormError(key, lengthKey)))
            case str if str.length < minLength => Left(Seq(FormError(key, lengthKey)))
            case str                           => Right(str)
          }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)

    }

  protected def validatedOptionalTextFormatter(invalidKey: String, lengthKey: String, regex: String, length: Int): Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
        data.get(key) match {
          case Some(str) if str.trim.isEmpty    => Right(None)
          case Some(str) if !str.matches(regex) => Left(Seq(FormError(key, invalidKey)))
          case Some(str) if str.length > length => Left(Seq(FormError(key, lengthKey)))
          case Some(str)                        => Right(Some(str))
          case _                                => Right(None)
        }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))

    }

  private[mappings] def stringTrimFormatter(errorKey: String, msgArg: String = ""): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None =>
          msgArg.isEmpty match {
            case true  => Left(Seq(FormError(key, errorKey)))
            case false => Left(Seq(FormError(key, errorKey, Seq(msgArg))))
          }
        case Some(s) =>
          s.trim match {
            case "" =>
              msgArg.isEmpty match {
                case true  => Left(Seq(FormError(key, errorKey)))
                case false => Left(Seq(FormError(key, errorKey, Seq(msgArg))))
              }
            case s1 => Right(removeNonBreakingSpaces(s1))
          }
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)

  }

  protected def validatedIdFormatter(requiredKey: String,
                                     invalidKey: String,
                                     invalidFormatKey: String,
                                     regex: String,
                                     msgArg: String = "",
                                     acceptedLengths: Seq[Int],
                                     isUtr: Boolean = false
  ): Formatter[String] =
    new Formatter[String] {

      def formatError(key: String, errorKey: String, msgArg: String): FormError =
        if (msgArg.isEmpty) FormError(key, errorKey) else FormError(key, errorKey, Seq(msgArg))

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        val trimmedId = if (isUtr) data.get(key).map(_.replaceAll("[kK\\s]", "")) else data.get(key).map(_.replaceAll("\\s", ""))
        trimmedId match {
          case None | Some("")                                => Left(Seq(formatError(key, requiredKey, msgArg)))
          case Some(s) if !s.matches(regex)                   => Left(Seq(formatError(key, invalidKey, msgArg)))
          case Some(s) if !acceptedLengths.contains(s.length) => Left(Seq(formatError(key, invalidFormatKey, msgArg)))
          case Some(s)                                        => Right(s)
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)

    }

  protected def validatedUrnFormatter(requiredKey: String, invalidKey: String, invalidFormatKey: String, msgArg: String = ""): Formatter[String] =
    new Formatter[String] {

      def formatError(key: String, errorKey: String, msgArg: String): FormError =
        if (msgArg.isEmpty) FormError(key, errorKey) else FormError(key, errorKey, Seq(msgArg))

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        val trimmedUrn = data.get(key).map(_.replaceAll("\\s", ""))
        trimmedUrn match {
          case None | Some("")                       => Left(Seq(formatError(key, requiredKey, msgArg)))
          case Some(s) if !s.matches(urnCharsRegex)  => Left(Seq(formatError(key, invalidKey, msgArg)))
          case Some(s) if !s.matches(urnFormatRegex) => Left(Seq(formatError(key, invalidFormatKey, msgArg)))
          case Some(s)                               => Right(s)
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)

    }

  private[mappings] def mandatoryPostcodeFormatter(requiredKey: String,
                                                   lengthKey: String,
                                                   invalidKey: String,
                                                   regex: String,
                                                   invalidCharKey: String,
                                                   validCharRegex: String
  ): Formatter[String] =
    new Formatter[String] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        val postCode          = postCodeDataTransform(data.get(key))
        val maxLengthPostcode = 10

        postCode match {
          case Some(postCode) if postCode.length > maxLengthPostcode            => Left(Seq(FormError(key, lengthKey)))
          case Some(postCode) if !stripSpaces(postCode).matches(validCharRegex) => Left(Seq(FormError(key, invalidCharKey)))
          case Some(postcode) if !stripSpaces(postcode).matches(regex)          => Left(Seq(FormError(key, invalidKey)))
          case Some(postcode)                                                   => Right(validPostCodeFormat(stripSpaces(postcode)))
          case _                                                                => Left(Seq(FormError(key, requiredKey)))
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)

    }

  private[mappings] def optionalPostcodeFormatter(lengthKey: String): Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val postCode          = postCodeDataTransform(data.get(key))
      val maxLengthPostcode = 10

      postCode match {
        case Some(postCode) if postCode.length > maxLengthPostcode => Left(Seq(FormError(key, lengthKey)))
        case Some(postcode)                                        => Right(Option(postcode))
        case _                                                     => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))

  }

  private[mappings] def mandatoryGIINFormatter(requiredKey: String,
                                               lengthKey: String,
                                               invalidKey: String,
                                               formatKey: String,
                                               invalidCharKey: String
  ): Formatter[String] =
    new Formatter[String] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        val giin      = data.get(key).map(validGIINFormat)
        val setLength = 19

        giin match {
          case None | Some("")                                 => Left(Seq(FormError(key, requiredKey)))
          case Some(value) if !value.matches(giinAllowedChars) => Left(Seq(FormError(key, invalidCharKey)))
          case Some(value) if value.length != setLength        => Left(Seq(FormError(key, lengthKey)))
          case Some(value) if !value.matches(invalidGIINRegex) => Left(Seq(FormError(key, formatKey)))
          case Some(value) if !value.matches(giinFormatRegex)  => Left(Seq(FormError(key, invalidKey)))
          case Some(value)                                     => Right(validGIINFormat(value))
          case _                                               => Left(Seq(FormError(key, invalidKey)))
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)

    }

}
