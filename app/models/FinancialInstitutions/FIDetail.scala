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

import play.api.libs.json.{Json, OFormat}

final case class FIDetail(
  FIID: String,
  FIName: String,
  SubscriptionID: String,
  TINDetails: TINDetails,
  IsFIUser: Boolean,
  IsFATCAReporting: Boolean,
  AddressDetails: AddressDetails,
  PrimaryContactDetails: ContactDetails,
  SecondaryContactDetails: ContactDetails
)

object FIDetail {
  implicit val format: OFormat[FIDetail] = Json.format[FIDetail]
}

object AddressDetails {
  implicit val format: OFormat[AddressDetails] = Json.format[AddressDetails]
}

final case class AddressDetails(
  AddressLine1: String,
  AddressLine2: String,
  AddressLine3: String,
  AddressLine4: Option[String],
  CountryCode: Option[String],
  PostalCode: Option[String]
)

final case class ContactDetails(ContactName: String, EmailAddress: String, PhoneNumber: String)

object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}
