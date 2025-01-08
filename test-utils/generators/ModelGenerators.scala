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

import models.FinancialInstitutions._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import utils.RegexConstants

trait ModelGenerators extends RegexConstants with Generators {

  implicit lazy val arbitraryTINType: Arbitrary[TINType] =
    Arbitrary {
      Gen.oneOf(TINType.allValues)
    }

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

  implicit lazy val arbitraryAddress: Arbitrary[models.Address] =
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
        country      <- arbitrary[Option[Country]]
      } yield AddressLookup(addressLine1, addressLine2, addressLine3, addressLine4, town, county, postCode, country)
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

  implicit val arbitraryAddressDetails: Arbitrary[AddressDetails] = Arbitrary {
    for {
      addressLine1 <- stringOfLength(35)
      addressLine2 <- Gen.option(stringOfLength(35))
      addressLine3 <- stringOfLength(35)
      addressLine4 <- Gen.option(stringOfLength(35))
      postalCode   <- Gen.option(validPostCodes)
      countryCode  <- stringOfLength(2)
    } yield AddressDetails(
      addressLine1,
      addressLine2,
      addressLine3,
      addressLine4,
      Option(countryCode.toUpperCase),
      postalCode
    )
  }

  implicit val arbitraryContactDetails: Arbitrary[ContactDetails] =
    Arbitrary {
      for {
        contactName  <- stringOfLength(105)
        emailAddress <- stringOfLength(105)
        phoneNumber  <- validPhoneNumber
      } yield ContactDetails(contactName, emailAddress + "@domain.com", Some(phoneNumber))
    }

  implicit val arbitraryTINDetails: Arbitrary[TINDetails] =
    Arbitrary {
      for {
        tinType  <- Gen.oneOf(TINType.allValues)
        tin      <- stringOfLength(25)
        issuedBy <- stringOfLength(2)
      } yield TINDetails(tinType, tin, issuedBy.toUpperCase)
    }

  implicit val arbitraryFIDetail: Arbitrary[FIDetail] = Arbitrary {
    for {
      fiId                    <- stringOfLength(15)
      fiName                  <- stringOfLength(105)
      subscriptionId          <- validSubscriptionID
      tinType                 <- Gen.oneOf(TINType.UTR, TINType.CRN, TINType.TRN, TINType.GIIN)
      tin                     <- stringOfLength(10)
      tinDetails              <- Gen.const(List(TINDetails(tinType, tin, "GB")))
      isFIUser                <- arbitrary[Boolean]
      isFATCAReporting        <- arbitrary[Boolean]
      addressDetails          <- arbitrary[AddressDetails]
      primaryContactDetails   <- arbitrary[ContactDetails]
      secondaryContactDetails <- Gen.option(arbitrary[ContactDetails])
    } yield FIDetail(
      FIID = fiId,
      FIName = fiName,
      SubscriptionID = subscriptionId,
      TINDetails = tinDetails,
      IsFIUser = isFIUser,
      IsFATCAReporting = isFATCAReporting,
      AddressDetails = addressDetails,
      PrimaryContactDetails = Some(primaryContactDetails),
      SecondaryContactDetails = secondaryContactDetails
    )
  }

  implicit val arbitraryUniqueTaxpayerReference: Arbitrary[UniqueTaxpayerReference] = Arbitrary {
    for {
      id <- arbitrary[String]
    } yield UniqueTaxpayerReference(id)
  }

  implicit val arbitraryGIIN: Arbitrary[GIINumber] = Arbitrary {
    arbitrary[String].map(GIINumber.apply)
  }

  // Line holder for template scripts
}
