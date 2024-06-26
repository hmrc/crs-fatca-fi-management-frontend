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

case class AddressResponse(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postalCode: Option[String],
  countryCode: String
) {

  def lines: Seq[String] = Seq(
    Some(addressLine1),
    addressLine2,
    addressLine3,
    addressLine4,
    postCodeFormatter(postalCode),
    getCountry(countryCode)
  ).flatten

  // to be extended to other countries in future
  private def getCountry(code: String): Option[String] =
    if (code == "GB") Some("United Kingdom") else Some(code)

  private def postCodeFormatter(postcode: Option[String]): Option[String] =
    postcode match {
      case Some(pc) =>
        val postCode = pc.replaceAll("\\s", "")
        val tail     = postCode.substring(postCode.length - 3)
        val head     = postCode.substring(0, postCode.length - 3)
        Some(s"$head $tail".toUpperCase)
      case _ => None
    }

}

object AddressResponse {

  implicit val format: OFormat[AddressResponse] = Json.format[AddressResponse]
}
