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

package connectors

import base.SpecBase
import generators.Generators
import helpers.WireMockServerHandler
import models.FinancialInstitutions.TINType.UTR
import models.FinancialInstitutions._
import models.RequestType.VIEW
import models.error.ApiError.{BadRequestError, JsValidationError, NoMatchingRecords, UnexpectedResponse}
import models.readFIs.response.{ResponseParameter, ViewFIDetailsResponse}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE, UNPROCESSABLE_ENTITY}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialInstitutionsConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckDrivenPropertyChecks with Generators {

  lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-fi-management.port" -> server.port(),
      "microservice.services.crs-fatca-fi-management.bearer-token" -> "local-token",
      "auditing.enabled"                                           -> "false"
    )
    .build()

  lazy val connector: FinancialInstitutionsConnector =
    app.injector.instanceOf[FinancialInstitutionsConnector]

  val createFIDetails = CreateFIDetails(
    FIName = "financial-institution",
    SubscriptionID = "XE512345678",
    TINDetails = Some(Seq(TINDetails(UTR, "TIN", "IssuedBy"))),
    GIIN = None,
    IsFIUser = true,
    AddressDetails = AddressDetails(
      AddressLine1 = "line 1",
      AddressLine2 = None,
      AddressLine3 = Some("line 3"),
      AddressLine4 = None,
      CountryCode = Some("GB"),
      PostalCode = Some("AA1 1AA")
    ),
    PrimaryContactDetails = Some(
      ContactDetails(
        ContactName = "contact-name",
        EmailAddress = "john.doe@test.com",
        PhoneNumber = None
      )
    ),
    SecondaryContactDetails = None
  )

  "FinancialInstitutionsConnector" - {

    "must return a ViewFIDetailsResponse for 200 status for viewFIs" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        OK,
        viewFIDetailsSuccessJson
      )
      val fidetailFuture: Future[Seq[FIDetail]] = connector.viewFis(subscriptionId)
      val result                                = fidetailFuture.futureValue

      result must have size 1
      val fiDetail = result.head
      fiDetail.FIID mustBe "683373339"
      fiDetail.FIName mustBe "Amazom UK"
      fiDetail.SubscriptionID mustBe "345567808"
      fiDetail.GIIN mustBe Some("123456234564456")
      fiDetail.IsFIUser mustBe false
      fiDetail.AddressDetails.AddressLine1 mustBe "22"
      fiDetail.AddressDetails.AddressLine2 mustBe Some("High Street")
      fiDetail.AddressDetails.AddressLine3 mustBe Some("Dawley")
      fiDetail.AddressDetails.AddressLine4 mustBe Some("Dawley")
      fiDetail.AddressDetails.CountryCode mustBe Some("GB")
      fiDetail.AddressDetails.PostalCode mustBe Some("TF22 2RE")
      fiDetail.PrimaryContactDetails mustBe Some(ContactDetails("John Smith", "jdoe@example.com", Some("789876568")))
      fiDetail.SecondaryContactDetails mustBe Some(ContactDetails("John Smith", "jdoe@example.com", Some("789876568")))
      fiDetail.TINDetails.get must contain theSameElementsAs Seq(
        TINDetails(TINType.UTR, "68936493", "GB")
      )
    }

    "must return an empty sequence when a 422 status code is return with error code 001 in viewFis" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        UNPROCESSABLE_ENTITY,
        unprocessible_entity_not_found_viewFiResponseJson
      )
      val result: Future[Seq[FIDetail]] = connector.viewFis(subscriptionId)
      result.futureValue must have size 0
    }

    "must return a JsValidationError when invalid json is return for a 200 response in viewFis" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        OK,
        """{"invalid": "json"}"""
      )
      val result = connector.viewFis(subscriptionId)
      result.failed.futureValue mustBe JsValidationError
    }

    "must return a UnexpectedError when a 400 status code is returned in viewFis" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        BAD_REQUEST,
        badRequest_viewFiResponseJson
      )
      val result = connector.viewFis(subscriptionId)
      result.failed.futureValue mustBe UnexpectedResponse
    }

    "must return a UnexpectedResponse when a unexpected status code is returned in viewFis" in new TestContext {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        INTERNAL_SERVER_ERROR,
        unexpectedErrorResponseJson
      )
      val result = connector.viewFis(subscriptionId)
      result.failed.futureValue mustBe UnexpectedResponse
    }

    "must return status as OK for viewFI" in new TestContext {
      forAll(validSubscriptionID, validFiId) {
        (subscriptionId, fiId) =>
          stubGetResponse(
            s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId",
            OK,
            viewFIDetailsSuccessJson
          )
          val result: Future[Option[FIDetail]] = connector.viewFi(subscriptionId, fiId)

          val fiDetail: FIDetail = result.futureValue.get
          fiDetail.FIID mustBe "683373339"
          fiDetail.FIName mustBe "Amazom UK"
          fiDetail.SubscriptionID mustBe "345567808"
          fiDetail.GIIN mustBe Some("123456234564456")
          fiDetail.IsFIUser mustBe false
          fiDetail.AddressDetails.AddressLine1 mustBe "22"
          fiDetail.AddressDetails.AddressLine2 mustBe Some("High Street")
          fiDetail.AddressDetails.AddressLine3 mustBe Some("Dawley")
          fiDetail.AddressDetails.AddressLine4 mustBe Some("Dawley")
          fiDetail.AddressDetails.CountryCode mustBe Some("GB")
          fiDetail.AddressDetails.PostalCode mustBe Some("TF22 2RE")
          fiDetail.PrimaryContactDetails mustBe Some(ContactDetails("John Smith", "jdoe@example.com", Some("789876568")))
          fiDetail.SecondaryContactDetails mustBe Some(ContactDetails("John Smith", "jdoe@example.com", Some("789876568")))
          fiDetail.TINDetails.get must contain theSameElementsAs Seq(
            TINDetails(TINType.UTR, "68936493", "GB")
          )
      }
    }

    "must return a None when a 422 status code is return with error code 001 in viewFi" in new TestContext {
      val subscriptionId = "XE512345678"
      val fiId           = "683373339"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId",
        UNPROCESSABLE_ENTITY,
        unprocessible_entity_not_found_viewFiResponseJson
      )
      val result = connector.viewFi(subscriptionId, fiId)
      result.futureValue mustBe None
    }

    "must return a JsValidationError when invalid json is return for a 200 response in viewFi" in new TestContext {
      val subscriptionId = "XE512345678"
      val fiId           = "683373339"
      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId",
        OK,
        """{"invalid": "json"}"""
      )
      val result = connector.viewFi(subscriptionId, fiId)
      result.failed.futureValue mustBe JsValidationError
    }

    "must return an unexpectedError when a 400 status code is returned in viewFi" in new TestContext {
      val subscriptionId = "XE512345678"
      val fiId           = "683373339"

      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId",
        BAD_REQUEST,
        badRequest_viewFiResponseJson
      )
      val result = connector.viewFi(subscriptionId, fiId)
      result.failed.futureValue mustBe UnexpectedResponse
    }

    "must return a UnexpectedResponse when a unexpected status code is returned in viewFi" in new TestContext {
      val subscriptionId = "XE512345678"
      val fiId           = "683373339"

      stubGetResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId",
        INTERNAL_SERVER_ERROR,
        unexpectedErrorResponseJson
      )
      val result = connector.viewFi(subscriptionId, fiId)
      result.failed.futureValue mustBe UnexpectedResponse
    }

    "addFi" - {

      "must return Right(HttpResponse) when the response status is OK" in {
        stubPostResponse(
          s"/crs-fatca-fi-management/financial-institutions/create",
          OK,
          "{}"
        )
        val result = connector.addFI(createFIDetails).futureValue
        result.status mustBe OK
      }

      "must return ErrorDetails and correct status code when the response status is not OK" in {
        val errorResponseJson =
          """{
            |"errorDetails": {
            |    "timestamp": "2016-08-16T18:15:41Z",
            |    "correlationId": "",
            |    "errorCode": "503"
            |}}""".stripMargin

        stubPostResponse(
          s"/crs-fatca-fi-management/financial-institutions/create",
          SERVICE_UNAVAILABLE,
          errorResponseJson
        )
        val result = connector.addFI(createFIDetails).futureValue

        result.status mustBe SERVICE_UNAVAILABLE
      }
    }

    "updateFI" - {
      "must return Right(HttpResponse) when the response status is OK" in {
        stubPutResponse(
          s"/crs-fatca-fi-management/financial-institutions/update",
          OK,
          "{}"
        )
        val result = connector.updateFI(testFiDetail).futureValue
        result.status mustBe OK
      }

      "must return ErrorDetails and correct status code when the response status is not OK" in {
        val errorResponseJson =
          """{
            |"errorDetails": {
            |    "timestamp": "2016-08-16T18:15:41Z",
            |    "correlationId": "",
            |    "errorCode": "503"
            |}}""".stripMargin

        stubPutResponse(
          s"/crs-fatca-fi-management/financial-institutions/update",
          SERVICE_UNAVAILABLE,
          errorResponseJson
        )
        val result = connector.updateFI(testFiDetail).futureValue

        result.status mustBe SERVICE_UNAVAILABLE
      }
    }

    "must return status as OK for removeFi" in {
      val removeFIDetail = RemoveFIDetail("FIID", "SubscriptionID")
      stubPostResponse(
        s"/crs-fatca-fi-management/financial-institutions/remove",
        OK,
        "{}"
      )
      val result = connector.removeFi(removeFIDetail)
      result.futureValue.status mustBe OK
    }

  }

  trait TestContext {

    val unexpectedErrorResponseJson = """{
                                        |  "errorDetail": {
                                        |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e",
                                        |    "errorCode": 405,
                                        |    "errorMessage": "<detail as generated by service>",
                                        |    "source": "Back End",
                                        |    "sourceFaultDetail": {
                                        |      "detail": [
                                        |        "<detail as generated by service>"
                                        |      ]
                                        |    },
                                        |    "timestamp": "2020-09-28T14:31:41.286Z"
                                        |  }
                                        |}""".stripMargin

    val unprocessible_entity_not_found_viewFiResponseJson = """{
                                                                |  "errorDetail": {
                                                                |    "correlationId": "1ae81b45-41b4-4642-ae1c-db1126900001",
                                                                |    "errorCode": "001",
                                                                |    "errorMessage": "No matching records found for the request",
                                                                |    "source": "journey-dct139b-service-camel",
                                                                |    "sourceFaultDetail": {
                                                                |      "detail": "001 - No matching records found for the request"
                                                                |    },
                                                                |    "timestamp": "2020-09-25T21:54:12.015Z"
                                                                |  }
                                                                |}""".stripMargin

    val badRequest_viewFiResponseJson = """{
                                           |  "errorDetail": {
                                           |    "correlationId": "1ae81b45-41b4-4642-ae1c-db1126900001",
                                           |    "errorCode": "400",
                                           |    "errorMessage": "Failed header validation",
                                           |    "source": "journey-dct139b-service-camel",
                                           |    "sourceFaultDetail": {
                                           |      "detail": [
                                           |        "Failed header validation: Invalid x-correlation-id header"
                                           |      ]
                                           |    },
                                           |    "timestamp": "2020-09-25T21:54:12.015Z"
                                           |  }
                                           |}""".stripMargin

    val viewFIDetailsSuccessJson = """{
                                |  "ViewFIDetails": {
                                |    "ResponseCommon": {
                                |      "OriginatingSystem": "CADX",
                                |      "Regime": "CRFA",
                                |      "RequestType": "VIEW",
                                |      "ResponseParameters": [
                                |        {
                                |          "ParamName": "FATCA1",
                                |          "ParamValue": "FATCA2"
                                |        }
                                |      ],
                                |      "TransmittingSystem": "EIS"
                                |    },
                                |    "ResponseDetails": {
                                |      "FIDetails": [
                                |        {
                                |          "AddressDetails": {
                                |            "AddressLine1": "22",
                                |            "AddressLine2": "High Street",
                                |            "AddressLine3": "Dawley",
                                |            "AddressLine4": "Dawley",
                                |            "CountryCode": "GB",
                                |            "PostalCode": "TF22 2RE"
                                |          },
                                |          "FIID": "683373339",
                                |          "FIName": "Amazom UK",
                                |          "GIIN": "123456234564456",
                                |          "IsFIUser": false,
                                |          "PrimaryContactDetails": {
                                |            "ContactName": "John Smith",
                                |            "EmailAddress": "jdoe@example.com",
                                |            "PhoneNumber": "789876568"
                                |          },
                                |          "SecondaryContactDetails": {
                                |            "ContactName": "John Smith",
                                |            "EmailAddress": "jdoe@example.com",
                                |            "PhoneNumber": "789876568"
                                |          },
                                |          "SubscriptionID": "345567808",
                                |          "TINDetails": [
                                |            {
                                |              "IssuedBy": "GB",
                                |              "TIN": "68936493",
                                |              "TINType": "UTR"
                                |            }
                                |          ]
                                |        }
                                |      ]
                                |    }
                                |  }
                                |}""".stripMargin

  }

}
