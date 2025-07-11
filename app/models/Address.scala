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

import play.api.libs.json._

case class Address(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postCode: Option[String],
  country: Country
) {

  def lines: Seq[String] = Seq(
    Some(addressLine1),
    addressLine2,
    addressLine3,
    addressLine4,
    postCode,
    Some(country.description)
  ).flatten

  def linesWithoutCountry: Seq[String] = Seq(
    Some(addressLine1),
    addressLine2,
    addressLine3,
    addressLine4,
    postCode
  ).flatten

  override def equals(obj: Any): Boolean = obj match {
    case that: Address =>
      this.addressLine1 == that.addressLine1 &&
      this.addressLine2 == that.addressLine2 &&
      this.addressLine3 == that.addressLine3 &&
      this.addressLine4 == that.addressLine4 &&
      this.postCode.map(_.trim.replaceAll(" ", "")) == that.postCode.map(_.trim.replaceAll(" ", "")) &&
      this.country.code == that.country.code
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(addressLine1, addressLine2, addressLine3, addressLine4, postCode, country.code)
    state
      .map(_.hashCode)
      .foldLeft(0)(
        (a, b) => 31 * a + b
      )
  }

}

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}
