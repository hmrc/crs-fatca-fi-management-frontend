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

package utils

import models.{Address, AddressLookup, AddressResponse}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

object AddressHelper {

  def formatAddress(address: AddressLookup): String = {
    val lines = Seq(
      address.addressLine1,
      address.addressLine2,
      address.addressLine3,
      address.addressLine4,
      address.town,
      address.postcode,
      address.county
    )
      .collect {
        case s: String => s
        case Some(s)   => s
      }
    lines.mkString(", ")
  }

  def formatAddress(address: Address): String = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4, address.postCode, address.country.description)
      .collect {
        case s: String => s
        case Some(s)   => s
      }
    lines.mkString(", ")
  }

  def formatAddressLookupBlock(address: AddressLookup): HtmlContent = {
    val lines = Seq(
      address.addressLine1,
      address.addressLine2,
      address.addressLine3,
      address.addressLine4,
      address.town,
      address.postcode,
      address.county,
      address.country.map(_.description)
    )
    toFormattedAddress(lines)
  }

  def formatAddressBlock(address: Address): HtmlContent = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4, address.postCode, address.country.description)
    toFormattedAddress(lines)
  }

  def formatAddressResponse(address: AddressResponse): HtmlContent =
    toFormattedAddress(address.linesWithoutCountry)

  private def toFormattedAddress(addressLines: Seq[Any]): HtmlContent = {
    val block = addressLines
      .collect {
        case s: String if s.nonEmpty       => s
        case Some(s: String) if s.nonEmpty => s
      }
      .map(
        line => s"<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${line.trim}</p>"
      )
      .mkString
    HtmlContent(block)
  }

}
