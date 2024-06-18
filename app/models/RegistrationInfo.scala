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

package models

import play.api.libs.json._

case class RegistrationInfo(safeId: SafeId, name: String, address: AddressResponse)

object RegistrationInfo {

  implicit val format: OFormat[RegistrationInfo] = Json.format[RegistrationInfo]
}

case class SafeId(value: String)

object SafeId {

  implicit val reads: Reads[SafeId] = __.read[String].map(SafeId.apply)

  implicit val writes: Writes[SafeId] = Writes(
    safeId => JsString(safeId.value)
  )

}

case class AddressResponse(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postalCode: Option[String],
  countryCode: String
)

object AddressResponse {

  implicit val format: Format[AddressResponse] = Json.format[AddressResponse]
}
