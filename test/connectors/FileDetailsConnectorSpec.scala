/*
 * Copyright 2026 HM Revenue & Customs
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
import models.IntenalIssueError
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}

import scala.concurrent.ExecutionContext.Implicits.global

class FileDetailsConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckDrivenPropertyChecks with Generators {

  lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-reporting.port" -> server.port(),
      "auditing.enabled" -> "false"
    )
    .build()

  lazy val connector: FileDetailsConnector =
    app.injector.instanceOf[FileDetailsConnector]

  "FileDetailsConnector" - {
    "checkSubscriptionHasRecentSubmissions should return true when the response is OK" in {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-reporting/files/details/$subscriptionId?page=1",
        OK,
        getAllFileDetailsStubResponse
      )

      val result = connector.checkSubscriptionHasRecentSubmissions(subscriptionId).futureValue
      result mustBe true
    }

    "checkSubscriptionHasRecentSubmissions should return false when the response is a notfound status code" in {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-reporting/files/details/$subscriptionId?page=1",
        NOT_FOUND,
        ""
      )
      val result = connector.checkSubscriptionHasRecentSubmissions(subscriptionId).futureValue
      result mustBe false
    }

    "checkSubscriptionHasRecentSubmissions should return false when the response is an internal server error" in {
      val subscriptionId = "XE512345678"
      stubGetResponse(
        s"/crs-fatca-reporting/files/details/$subscriptionId?page=1",
        INTERNAL_SERVER_ERROR,
        ""
      )
      val result = connector.checkSubscriptionHasRecentSubmissions(subscriptionId)
      result.failed.futureValue mustBe an[IntenalIssueError.type]
    }
  }

  private def getAllFileDetailsStubResponse: String =
    """
      |{
      |"fileDetailsList": [
      |{
      |  "_id": "conversation-123",
      |  "enrolmentId": "XACBC0000123456",
      |  "messageRefId": "GBXACBC12345678",
      |  "reportingEntityName": "Test Entity",
      |  "status": {"Pending":{}},
      |  "name": "test-file.xml",
      |  "submitted": "2026-01-06T12:00:00",
      |  "lastUpdated": "2026-01-06T12:00:00",
      |  "reportingPeriod": "2026-01-01",
      |  "messageType": "CRS",
      |  "reportType": "TEST_DATA",
      |  "fiNameFromFim": "Test FI Name",
      |  "isFiUser": true,
      |  "fileType":"NormalFile",
      |  "fiPrimaryContactEmail":"fiPrimary@email.com",
      |  "fiSecondaryContactEmail":"fiSecondary@email.com",
      |  "subscriptionPrimaryContactEmail":"test@email.com",
      |  "subscriptionSecondaryContactEmail":"secondarySub@email.com",
      |  "sendCompanyIn":"some-company-in"
      |}
      |],
      |"pages": 1
      |}
      |""".stripMargin

}
