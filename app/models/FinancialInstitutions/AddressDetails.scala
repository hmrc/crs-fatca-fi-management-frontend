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

package models.FinancialInstitutions

import models.Address
import play.api.libs.json._
import utils.CountryListFactory

object AddressDetails {
  implicit val format: OFormat[AddressDetails] = Json.format[AddressDetails]

  implicit class AddressDetailsExtension(addressDetails: AddressDetails) {

    def toAddress(factory: CountryListFactory): Option[Address] =
      for {
        countryCode <- addressDetails.CountryCode
        country     <- factory.findCountryWithCode(countryCode)
      } yield Address(
        addressLine1 = addressDetails.AddressLine1,
        addressLine2 = addressDetails.AddressLine2,
        addressLine3 = addressDetails.AddressLine3,
        addressLine4 = addressDetails.AddressLine4,
        postCode = addressDetails.PostalCode,
        country = country
      )

  }

}

final case class AddressDetails(
  AddressLine1: String,
  AddressLine2: Option[String],
  AddressLine3: Option[String],
  AddressLine4: Option[String],
  CountryCode: Option[String],
  PostalCode: Option[String]
)
