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

import base.SpecBase
import models.{Address, AddressLookup, AddressResponse, Country}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class AddressHelperSpec extends SpecBase {
  val sut = AddressHelper

  "formatAddress" - {
    "format Address correctly" in {
      val address = Address("line1", Some("line2"), "line3", Some("line4"), Some("postcode"), Country.GB)
      val result  = sut.formatAddress(address)
      result mustBe "line1, line2, line3, line4, postcode, United Kingdom"
    }
    "format Address correctly as html" in {
      val address = Address("line1", Some("line2"), "line3", Some("line4"), Some("postcode"), Country.GB)
      val expectedResult = HtmlContent(
        """|<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line1</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line2</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line3</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line4</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>postcode</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>United Kingdom</p>
           |""".stripMargin
      )
      val result = sut.formatAddressBlock(address)
      result.equals(expectedResult)
    }

    "format the address from AddressLookup correctly" in {
      val address = AddressLookup(Some("line1"), Some("line2"), Some("line3"), Some("line4"), "town", Some("county"), "postcode", Some("country"))
      val result  = sut.formatAddress(address)
      result mustBe "line1, line2, line3, line4, town, postcode, county, country"
    }

    "format the address from AddressResponse correctly as html" in {
      val address = AddressResponse("line1", Some("line2"), Some("line3"), Some("line4"), Some("ab12cd"), "GB")
      val expectedResult = HtmlContent(
        """|<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line1</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line2</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line3</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>line4</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>AB1 2CD</p>
           |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>United Kingdom</p>
           |""".stripMargin
      )
      val result = sut.formatAddressResponse(address)
      result.equals(expectedResult)
    }

    "must format AddressLookupBlock as html" in {
      val addressLookup = AddressLookup(Some("line1"), Some("line2"), Some("line3"), Some("line4"), "town", Some("county"), "postcode", Some("country"))

      val result = sut.formatAddressLookupBlock(addressLookup)

      val formattedAddress = HtmlContent(
        s"""|<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.addressLine1.value}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.addressLine2.value}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.addressLine3.value}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.addressLine4.value}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.town}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.postcode}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.county.value}</p>
               |<p class='govuk-!-margin-top-0 govuk-!-margin-bottom-0'>${addressLookup.country.value}</p>
               |""".stripMargin.replaceAll("\\n", "")
      )
      result mustBe formattedAddress
    }

  }

}
