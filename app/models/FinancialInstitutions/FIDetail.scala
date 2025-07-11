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
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.CountryListFactory

sealed trait BaseFIDetail {
  val FIName: String
  val SubscriptionID: String
  val TINDetails: Seq[TINDetails]
  val GIIN: Option[String]
  val IsFIUser: Boolean
  val AddressDetails: AddressDetails
  val PrimaryContactDetails: Option[ContactDetails]
  val SecondaryContactDetails: Option[ContactDetails]
}

object BaseFIDetail {
  implicit val format: OFormat[BaseFIDetail] = Json.format[BaseFIDetail]
}

final case class FIDetail(
  FIID: String,
  FIName: String,
  SubscriptionID: String,
  TINDetails: Seq[TINDetails],
  GIIN: Option[String],
  IsFIUser: Boolean,
  AddressDetails: AddressDetails,
  PrimaryContactDetails: Option[ContactDetails],
  SecondaryContactDetails: Option[ContactDetails]
) extends BaseFIDetail

object FIDetail {

  implicit val reads: Reads[FIDetail] = (
    (JsPath \ "FIID").read[String] and
      (JsPath \ "FIName").read[String] and
      (JsPath \ "SubscriptionID").read[String] and
      (JsPath \ "TINDetails").readNullable[Seq[TINDetails]].map(_.getOrElse(Seq.empty)) and
      (JsPath \ "GIIN").readNullable[String] and
      (JsPath \ "IsFIUser").read[Boolean] and
      (JsPath \ "AddressDetails").read[AddressDetails] and
      (JsPath \ "PrimaryContactDetails").readNullable[ContactDetails] and
      (JsPath \ "SecondaryContactDetails").readNullable[ContactDetails]
  )(FIDetail.apply _)

  implicit val writes: OWrites[FIDetail] = Json.writes[FIDetail]
  implicit val format: OFormat[FIDetail] = OFormat(reads, writes)
}

final case class RemoveFIDetail(
  SubscriptionID: String,
  FIID: String
)

object RemoveFIDetail {
  implicit val format: OFormat[RemoveFIDetail] = Json.format[RemoveFIDetail]
}

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

final case class ContactDetails(ContactName: String, EmailAddress: String, PhoneNumber: Option[String])

object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

final case class CreateFIDetails(
  FIName: String,
  SubscriptionID: String,
  TINDetails: Seq[TINDetails],
  GIIN: Option[String],
  IsFIUser: Boolean,
  AddressDetails: AddressDetails,
  PrimaryContactDetails: Option[ContactDetails],
  SecondaryContactDetails: Option[ContactDetails]
) extends BaseFIDetail

object CreateFIDetails {
  implicit val format: OFormat[CreateFIDetails] = Json.format[CreateFIDetails]
}

case class SubmitFIDetailsResponse(fiid: Option[String])

object SubmitFIDetailsResponse {

  implicit val reads: Reads[SubmitFIDetailsResponse] = (json: JsValue) => {
    val fiid = (json \ "ResponseDetails" \ "ReturnParameters" \ "Value").asOpt[String]
    JsSuccess(SubmitFIDetailsResponse(fiid))
  }

}
