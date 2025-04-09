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
import models.FinancialInstitutions.{AddressDetails, ContactDetails, CreateFIDetails, RemoveFIDetail}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}

import scala.concurrent.ExecutionContext.Implicits.global

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
    TINDetails = Seq.empty,
    GIIN = None,
    IsFIUser = true,
    AddressDetails = AddressDetails(
      AddressLine1 = "line 1",
      AddressLine2 = None,
      AddressLine3 = "line 3",
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

    "must return status as OK for viewFIs" in {
      val subscriptionId = "XE512345678"
      stubResponse(
        s"/crs-fatca-fi-management/financial-institutions/$subscriptionId",
        OK
      )
      val result = connector.viewFis(subscriptionId)
      result.futureValue.status mustBe OK
    }

    "must return status as OK for viewFI" in {
      forAll(validSubscriptionID, validFiId) {
        (subscriptionId, fiId) =>
          stubResponse(
            s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId",
            OK
          )
          val result = connector.viewFi(subscriptionId, fiId)
          result.futureValue.status mustBe OK
      }
    }

    "addFi" - {

      "must return Right(HttpResponse) when the response status is OK" in {
        stubPostResponse(
          s"/crs-fatca-fi-management/financial-institutions/create",
          OK,
          "{}"
        )
        val result = connector.addOrUpdateFI(createFIDetails).futureValue
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
        val result = connector.addOrUpdateFI(createFIDetails).futureValue

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

}
