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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.IdentifierType
import models.subscription.request.ReadSubscriptionRequest
import models.subscription.response.DisplaySubscriptionResponse
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-registration.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]
  val readSubscriptionUrl                   = "/crs-fatca-registration/subscription/read-subscription"

  val exampleResponse = s"""{
    "success": {
    "crfaSubscriptionDetails": {
        "processingDate": "2023-05-17T09:26:17Z",
        "crfaReference": "[subscriptionId]",
        "tradingName": "James Hank",
        "gbUser": true,
        "primaryContact": {
        "organisation": {
        "name": "Mark Ltd"
      },
        "email": "james@test.com",
        "phone": "0202731454",
        "mobile": "07896543333"
      }
      }
    }
  }""".stripMargin

  "Subscription Connector" - {
    "Return subscription response" in {
      stubResponse(readSubscriptionUrl, OK, exampleResponse)

      val result = connector.readSubscription(ReadSubscriptionRequest(IdentifierType.FATCAID, "fatcaId"))
      result.futureValue mustBe Json.parse(exampleResponse).as[DisplaySubscriptionResponse]
    }

    "Throw an exception when display response doesn't contain subscription information" in {
      stubResponse(readSubscriptionUrl, OK, "{}")

      val result = connector.readSubscription(ReadSubscriptionRequest(IdentifierType.FATCAID, "fatcaId"))
      assertThrows[Exception] {
        result.futureValue
      }
    }

    "Throw an exception when response status is not 2XX" in {
      stubResponse(readSubscriptionUrl, INTERNAL_SERVER_ERROR, exampleResponse)

      val result = connector.readSubscription(ReadSubscriptionRequest(IdentifierType.FATCAID, "fatcaId"))
      assertThrows[Exception] {
        result.futureValue
      }
    }

    "Throw an exception when response status is not 2XX and response doesn't contain subscription information" in {
      stubResponse(readSubscriptionUrl, INTERNAL_SERVER_ERROR, "{}")

      val result = connector.readSubscription(ReadSubscriptionRequest(IdentifierType.FATCAID, "fatcaId"))
      assertThrows[Exception] {
        result.futureValue
      }
    }
  }

  private def stubResponse(expectedUrl: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
