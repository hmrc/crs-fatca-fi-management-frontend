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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class LookupAddressByPostcode(postcode: String, filter: Option[String])

case class AddressLookup(addressLine1: Option[String],
                         addressLine2: Option[String],
                         addressLine3: Option[String],
                         addressLine4: Option[String],
                         town: String,
                         county: Option[String],
                         postcode: String,
                         country: Option[Country]
) {

  val toAddress: Address = {

    val line1 = addressLine1.getOrElse("")
    val line2 = addressLine2
    val line3 = addressLine3
      .getOrElse(town)
    val line4 = (addressLine3.isEmpty, addressLine4.isEmpty) match {
      case (true, true)  => county
      case (false, true) => Some(town)
      case (_, _)        => addressLine4
    }
    val safePostcode = Option(postcode)

    Address(line1, line2, Some(line3), line4, safePostcode, country.getOrElse(Country.GB))
  }

}

object LookupAddressByPostcode {
  implicit val writes: Writes[LookupAddressByPostcode] = Json.writes[LookupAddressByPostcode]
}

object AddressLookup {

  implicit val addressLookupWrite: Writes[AddressLookup] = (addressLookup: AddressLookup) => {
    def lines: List[String] = List(addressLookup.addressLine1, addressLookup.addressLine2, addressLookup.addressLine3, addressLookup.addressLine4).flatten

    Json.obj(
      "address" ->
        Json.obj(
          "lines"    -> lines,
          "town"     -> addressLookup.town,
          "county"   -> addressLookup.county,
          "postcode" -> addressLookup.postcode,
          "country"  -> addressLookup.country
        )
    )
  }

  implicit val addressLookupReads: Reads[AddressLookup] =
    ((JsPath \ "address" \ "lines").read[List[String]] and
      (JsPath \ "address" \ "town").read[String] and
      (JsPath \ "address" \ "county").readNullable[String] and
      (JsPath \ "address" \ "postcode").read[String] and
      (JsPath \ "address" \ "country" \ "code").readNullable[String] and
      (JsPath \ "address" \ "country" \ "description").readNullable[String] and
      (JsPath \ "address" \ "country" \ "name").readNullable[String]) {
      (lines, town, county, postcode, countryCode, countryDescription, countryName) =>
        val addressLines: (Option[String], Option[String], Option[String], Option[String]) =
          lines.size match {
            case 0 =>
              (None, None, None, None)
            case 1 =>
              (Some(lines.head), None, None, None)
            case 2 =>
              (Some(lines.head), None, Some(lines(1)), None)
            case 3 =>
              (Some(lines.head), Some(lines(1)), Some(lines(2)), None)
            case numberOfLines if numberOfLines >= 4 => (Some(lines.head), Some(lines(1)), Some(lines(2)), Some(lines(3)))
          }
        AddressLookup(
          addressLines._1,
          addressLines._2,
          addressLines._3,
          addressLines._4,
          town,
          county,
          postcode,
          countryCode.map(
            code => Country("", code, countryDescription.orElse(countryName).getOrElse(code))
          )
        )
    }

  implicit val addressesLookupReads: Reads[Seq[AddressLookup]] = Reads {
    json =>
      json.validate[Seq[JsValue]] flatMap {
        _.foldLeft[JsResult[List[AddressLookup]]](JsSuccess(List.empty)) {
          (addresses, currentAddress) =>
            for {
              sequenceOfAddresses <- addresses
              address             <- currentAddress.validate[AddressLookup](addressLookupReads)
            } yield sequenceOfAddresses :+ address
        }
      }
  }

}
