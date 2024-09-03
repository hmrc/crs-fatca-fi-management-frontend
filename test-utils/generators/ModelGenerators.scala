/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import utils.RegexConstants

trait ModelGenerators extends RegexConstants with Generators {

  val maximumNumber     = 999999
  val minimumNumber     = 1
  val countryNumber     = 2
  val EmailLength       = 132
  val PhoneNumberLength = 24
  val MaxNameLength     = 35

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        state <- Gen.oneOf(Seq("Valid", "Invalid"))
        code  <- Gen.pick(countryNumber, 'A' to 'Z')
        name  <- arbitrary[String]
      } yield Country(state, code.mkString, name)
    }

  implicit lazy val arbitraryAddressWithoutId: Arbitrary[models.Address] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String].suchThat(_.nonEmpty)
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[String].suchThat(_.nonEmpty)
        addressLine4 <- arbitrary[Option[String]]
        postCode     <- arbitrary[Option[String]]
        country      <- arbitrary[Country]
      } yield Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode, country)
    }

  implicit lazy val arbitraryAddressLookup: Arbitrary[models.AddressLookup] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[Option[String]]
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[Option[String]]
        addressLine4 <- arbitrary[Option[String]]
        postCode     <- arbitrary[String]
        town         <- arbitrary[String]
        county       <- arbitrary[Option[String]]
      } yield AddressLookup(addressLine1, addressLine2, addressLine3, addressLine4, town, county, postCode)
    }

  implicit val arbitraryAddressResponse: Arbitrary[AddressResponse] = Arbitrary {
    val postCode = for {
      size     <- Gen.chooseNum(5, 7)
      postCode <- Gen.option(Gen.listOfN(size, Gen.alphaNumChar).map(_.mkString))
    } yield postCode
    for {
      addressline  <- arbitrary[String]
      addressline2 <- arbitrary[Option[String]]
      addressline3 <- arbitrary[Option[String]]
      addressline4 <- arbitrary[Option[String]]
      postcode     <- postCode
      countrycode  <- arbitrary[String]
    } yield AddressResponse(addressline, addressline2, addressline3, addressline4, postcode, countrycode)
  }

//Line holder for template scripts
  implicit val arbitraryUniqueTaxpayerReference: Arbitrary[UniqueTaxpayerReference] = Arbitrary {
    for {
      id <- arbitrary[String]
    } yield UniqueTaxpayerReference(id)
  }

  // Line holder for template scripts
}
