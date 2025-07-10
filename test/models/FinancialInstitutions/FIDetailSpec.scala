/*
 * Copyright 2025 HM Revenue & Customs
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

import models.FinancialInstitutions.TINType.UTR
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class FIDetailSpec extends AnyFreeSpec with Matchers {

  "FIDetail" - {
    "must unmarshal TINDetails correctly when null" in {
      val json =
        """
          |{
          |  "FIID": "683373339",
          |  "FIName": "First FI",
          |  "SubscriptionID": "[subscriptionId]",
          |  "IsFIUser": true,
          |  "AddressDetails": {
          |    "AddressLine1": "2 High Street",
          |    "AddressLine3": "Birmingham",
          |    "CountryCode": "GB",
          |    "PostalCode": "BA23 2AZ"
          |  },
          |  "PrimaryContactDetails": {
          |    "ContactName": "Jane Doe",
          |    "EmailAddress": "janedoe@example.com",
          |    "PhoneNumber": "0444458888"
          |  }
          |}
          |""".stripMargin

      val expected = FIDetail(
        FIID = "683373339",
        FIName = "First FI",
        SubscriptionID = "[subscriptionId]",
        TINDetails = Seq.empty,
        GIIN = None,
        IsFIUser = true,
        AddressDetails = AddressDetails(
          AddressLine1 = "2 High Street",
          AddressLine2 = None,
          AddressLine3 = Some("Birmingham"),
          AddressLine4 = None,
          CountryCode = Some("GB"),
          PostalCode = Some("BA23 2AZ")
        ),
        PrimaryContactDetails = Some(ContactDetails("Jane Doe", "janedoe@example.com", Some("0444458888"))),
        SecondaryContactDetails = None
      )

      FIDetail.format.reads(Json.parse(json)) mustEqual JsSuccess(expected)
    }

    "must unmarshal TINDetails correctly when not null" in {
      val json =
        """
          |{
          |  "FIID": "683373339",
          |  "FIName": "First FI",
          |  "SubscriptionID": "[subscriptionId]",
          |  "TINDetails": [
          |    {
          |      "TINType": "UTR",
          |      "TIN": "1234567890",
          |      "IssuedBy": "GB"
          |    }
          |  ],
          |  "IsFIUser": true,
          |  "AddressDetails": {
          |    "AddressLine1": "2 High Street",
          |    "AddressLine3": "Birmingham",
          |    "CountryCode": "GB",
          |    "PostalCode": "BA23 2AZ"
          |  },
          |  "PrimaryContactDetails": {
          |    "ContactName": "Jane Doe",
          |    "EmailAddress": "janedoe@example.com",
          |    "PhoneNumber": "0444458888"
          |  }
          |}
          |""".stripMargin

      val expected = FIDetail(
        FIID = "683373339",
        FIName = "First FI",
        SubscriptionID = "[subscriptionId]",
        TINDetails = Seq(TINDetails(UTR, "1234567890", "GB")),
        GIIN = None,
        IsFIUser = true,
        AddressDetails = AddressDetails(
          AddressLine1 = "2 High Street",
          AddressLine2 = None,
          AddressLine3 = Some("Birmingham"),
          AddressLine4 = None,
          CountryCode = Some("GB"),
          PostalCode = Some("BA23 2AZ")
        ),
        PrimaryContactDetails = Some(ContactDetails("Jane Doe", "janedoe@example.com", Some("0444458888"))),
        SecondaryContactDetails = None
      )

      FIDetail.format.reads(Json.parse(json)) mustEqual JsSuccess(expected)
    }
  }

}
