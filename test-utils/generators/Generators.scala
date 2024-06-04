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

package generators

import java.time.{Instant, LocalDate, ZoneOffset}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Gen, Shrink}
import utils.RegexConstants
import wolfendale.scalacheck.regexp.RegexpGen

trait Generators extends RegexConstants {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield seq1.toSeq.zip(seq2).foldLeft("") {
      case (acc, (n, Some(v))) =>
        acc + n + v
      case (acc, (n, _)) =>
        acc + n
    }
  }

  def stringsLongerThanAlpha(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * 2).max(100)
    length    <- Gen.chooseNum(minLength + 1, maxLength)
    chars     <- listOfN(length, Gen.alphaChar)
  } yield chars.mkString

  def stringsShorterThanAlpha(minLength: Int): Gen[String] = for {
    lowestPossibleLength <- 1
    length               <- Gen.chooseNum(lowestPossibleLength, minLength - 1)
    chars                <- listOfN(length, Gen.alphaChar)
  } yield chars.mkString

  def stringMatchingRegexAndLength(regex: String, length: Int): Gen[String] =
    RegexpGen
      .from(regex)
      .suchThat(
        value => value.trim.nonEmpty
      )
      .map(_.take(length))

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max).map(_.toString)
    genIntersperseString(numberGen, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (
      x => x > Int.MaxValue
    )

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (
      x => x < Int.MinValue
    )

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map("%f".format(_))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (
      x => x < min || x > max
    )

  def validPhoneNumber(ln: Int): Gen[String] = for {
    length <- Gen.chooseNum(1, ln - 1)
    chars  <- listOfN(length, Gen.chooseNum(0, 9))
  } yield "+" + chars.mkString

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsLongerThan(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * 2).max(100)
    length    <- Gen.chooseNum(minLength + 1, maxLength)
    chars     <- listOfN(length, arbitrary[Char])
  } yield chars.mkString

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def validPhoneNumberTooLong(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * 2).max(100)
    length    <- Gen.chooseNum(minLength + 1, maxLength)
    chars     <- listOfN(length, arbitrary[Byte])
  } yield chars.map(math.abs(_)).mkString

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  def stringsNotOfFixedLengthNumeric(givenLength: Int): Gen[String] = for {
    maxLength <- givenLength + 50
    length    <- Gen.chooseNum(1, maxLength).suchThat(_ != givenLength)
    chars     <- listOfN(length, Gen.numChar)
  } yield chars.mkString

  def validUtr: Gen[String] = for {
    chars <- listOfN(10, Gen.oneOf(List(1, 2, 3, 4, 5, 6, 7, 8, 9)))
  } yield chars.mkString

  def validPostCodes: Gen[String] = {
    val disallowed = List('c', 'i', 'k', 'm', 'o', 'v')
    for {
      pt1Quantity <- Gen.choose(1, 2)
      pt1         <- Gen.listOfN(pt1Quantity, Gen.alphaChar).map(_.mkString)
      pt2         <- Gen.choose(0, 9)

      pt3numOpt <- Gen.option(Gen.choose(0, 9))
      pt3 = pt3numOpt.getOrElse("").toString

      pt4 <- Gen.choose(0, 9)
      pt5a <- Gen.alphaChar suchThat (
        ch => !disallowed.contains(ch.toLower)
      )
      pt5b <- Gen.alphaChar suchThat (
        ch => !disallowed.contains(ch.toLower)
      )
    } yield s"$pt1$pt2$pt3 $pt4$pt5a$pt5b"
  }

  def validGIIN: Gen[String] = {
    val disallowed = 'o'
    for {
      pt1 <- Gen.listOfN(6, Gen.alphaNumChar).map(_.mkString) suchThat (
        ch => !ch.toLowerCase.contains(disallowed)
      )
      pt2 <- Gen.listOfN(5, Gen.alphaNumChar).map(_.mkString)
      pt3 <- Gen.listOfN(2, Gen.alphaNumChar).map(_.mkString)
      pt4 <- Gen.listOfN(3, Gen.alphaNumChar).map(_.mkString)
    } yield s"$pt1.$pt2.$pt3.$pt4"
  }

  def invalidCountry: Gen[String] = Gen.oneOf(Set("Invalid Country 1", "Invalid Country 2", "&nbsp"))

}
