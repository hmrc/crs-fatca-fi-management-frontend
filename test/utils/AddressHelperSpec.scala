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
import models.{Address, AddressLookup, Country}

class AddressHelperSpec extends SpecBase {
  val sut = AddressHelper

  "formatAddress" - {
    "format the address correctly" in {
      val address = Address("line1", Some("line2"), "line3", Some("line4"), Some("postcode"), Country.GB)
      val result  = sut.formatAddress(address)
      result mustBe "line1, line2, line3, line4, postcode, United Kingdom"
    }

    "format the address from AddressLookup correctly" in {
      val address = AddressLookup(Some("line1"), Some("line2"), Some("line3"), Some("line4"), "town", Some("county"), "postcode")
      val result  = sut.formatAddress(address)
      result mustBe "line1, line2, line3, line4, town, postcode, county"
    }

  }

}
