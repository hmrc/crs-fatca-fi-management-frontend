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

package services

import base.SpecBase
import connectors.FinancialInstitutionsConnector
import generators.{ModelGenerators, UserAnswersGenerator}
import models.FinancialInstitutions.TINType.GIIN
import models.FinancialInstitutions.{AddressDetails, ContactDetails, FIDetail, TINDetails}
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsServiceSpec extends SpecBase with ModelGenerators with UserAnswersGenerator with ScalaCheckPropertyChecks {

  val mockConnector: FinancialInstitutionsConnector = mock[FinancialInstitutionsConnector]
  val sut                                           = new FinancialInstitutionsService(mockConnector)

  implicit override val hc: HeaderCarrier = HeaderCarrier()

  "FinancialInstitutionsService" - {

    "getListOfFinancialInstitutions extracts list of FI details" in {
      val subscriptionId = "XE5123456789"
      val mockResponse   = Future.successful(HttpResponse(OK, viewFIDetailsBody))

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = sut.getListOfFinancialInstitutions(subscriptionId)
      result.futureValue mustBe fiDetails
    }

    "addFinancialInstitution adds FI details" in {
      val mockResponse   = Future.successful(HttpResponse(OK, "{}"))
      val subscriptionId = "XE5123456789"
      forAll(fiNotRegistered.arbitrary) {
        (userAnswers: UserAnswers) =>
          when(mockConnector.addFi(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(mockResponse)
          val result = sut.addFinancialInstitution(subscriptionId, userAnswers)
          result.futureValue mustBe ()
      }

    }

  }

  val fiDetails: Seq[FIDetail] =
    Seq(
      FIDetail(
        "683373339",
        "First FI",
        "[subscriptionId]",
        List(TINDetails(GIIN, "689355555", "GB")),
        true,
        true,
        AddressDetails("22", Some("High Street"), "Dawley", Some("Dawley"), Some("GB"), Some("TF22 2RE")),
        ContactDetails("Jane Doe", "janedoe@example.com", Some("0444458888")),
        Some(ContactDetails("John Doe", "johndoe@example.com", Some("0333458888")))
      ),
      FIDetail(
        "683373300",
        "Second FI",
        "[subscriptionId]",
        List(TINDetails(GIIN, "689344444", "GB")),
        true,
        true,
        AddressDetails("22", Some("High Street"), "Dawley", Some("Dawley"), Some("GB"), Some("TF22 2RE")),
        ContactDetails("Foo Bar", "fbar@example.com", Some("0223458888")),
        Some(ContactDetails("Foobar Baz", "fbaz@example.com", Some("0123456789")))
      )
    )

  val viewFIDetailsBody = """{
    "ViewFIDetails": {
      "ResponseDetails": {
        "FIDetails": [
          {
            "FIID": "683373339",
            "FIName": "First FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [
              {
                "TINType": "GIIN",
                "TIN": "689355555",
                "IssuedBy": "GB"
              }
            ],
            "IsFIUser": true,
            "IsFATCAReporting": true,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "High Street",
              "AddressLine3": "Dawley",
              "AddressLine4": "Dawley",
              "CountryCode": "GB",
              "PostalCode": "TF22 2RE"
            },
            "PrimaryContactDetails": {
              "ContactName": "Jane Doe",
              "EmailAddress": "janedoe@example.com",
              "PhoneNumber": "0444458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "John Doe",
              "EmailAddress": "johndoe@example.com",
              "PhoneNumber": "0333458888"
            }
          },
          {
            "FIID": "683373300",
            "FIName": "Second FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [
              {
                "TINType": "GIIN",
                "TIN": "689344444",
                "IssuedBy": "GB"
              }
            ],
            "IsFIUser": true,
            "IsFATCAReporting": true,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "High Street",
              "AddressLine3": "Dawley",
              "AddressLine4": "Dawley",
              "CountryCode": "GB",
              "PostalCode": "TF22 2RE"
            },
            "PrimaryContactDetails": {
              "ContactName": "Foo Bar",
              "EmailAddress": "fbar@example.com",
              "PhoneNumber": "0223458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "Foobar Baz",
              "EmailAddress": "fbaz@example.com",
              "PhoneNumber": "0123456789"
            }
          }
        ]
      }
    }
  }
"""
}
