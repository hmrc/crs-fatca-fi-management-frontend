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
import helpers.WireMockServerHandler
import models.FinancialInstitutions.{AddressDetails, ContactDetails, CreateFIDetails, RemoveFIDetail}
import models.response.ErrorDetails
import play.api.Application
import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialInstitutionsConnectorSpec extends SpecBase with WireMockServerHandler {

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
    IsFIUser = true,
    IsFATCAReporting = false,
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

    "addFi" - {

      "must return Right(HttpResponse) when the response status is OK" in {
        stubPostResponse(
          s"/crs-fatca-fi-management/financial-institutions/create",
          OK,
          "{}"
        )
        val result = connector.addFi(createFIDetails).futureValue
        result.equals(Future(Right(HttpResponse(OK))))
      }

      "return Left(ErrorDetails) when the response status is not OK" in {
//todo: fix
        val fiDetails = createFIDetails

        val errorResponseJson =
          """{
            |"ErrorDetails": {
            |    "timestamp": "2016-08-16T18:15:41Z",
            |    "correlationId": "",
            |    "errorCode": "503"
            |}}""".stripMargin

        stubPostResponse(
          s"/crs-fatca-fi-management/financial-institutions/create",
          SERVICE_UNAVAILABLE,
          errorResponseJson
        )
        val result = connector.addFi(fiDetails).futureValue

        result mustBe a[Left[_, _]]
        result.left.get mustBe ErrorDetails("2016-08-16T18:15:41Z", "", Some("503"))
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
