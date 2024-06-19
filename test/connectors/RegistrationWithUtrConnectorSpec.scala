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
import models.UniqueTaxpayerReference
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationWithUtrConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  lazy val application: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.crs-fatca-registration.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationWithUtrConnector = application.injector.instanceOf[RegistrationWithUtrConnector]
  val endpoint                                     = "/organisation/utr-only"

  "sendAndRetrieveRegWithUtr" - {
    val utr = UniqueTaxpayerReference("1112223330")

    "return 200 and a registration response when organisation is matched by utr" in {

      stubResponse(endpoint, OK, orgRegWithUtrResponse)
      val result = connector.sendAndRetrieveRegWithUtr(utr)
      result.futureValue.status mustBe 200
      result.futureValue.body mustBe orgRegWithUtrResponse
    }

    "return 404 and NotFoundError when there is no match" in {

      stubResponse(endpoint, NOT_FOUND, orgRegWithUtrResponse)

      val result = connector.sendAndRetrieveRegWithUtr(utr)
      assertThrows[Exception] {
        result.futureValue
      }

    }

    "return 503 and ServiceUnavailableError when remote is unavailable " in {

      stubResponse(endpoint, SERVICE_UNAVAILABLE, orgRegWithUtrResponse)

      val result = connector.sendAndRetrieveRegWithUtr(utr)
      assertThrows[Exception] {
        result.futureValue
      }

    }
  }

  private def stubResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping = {
    val registrationUrl = "/crs-fatca-registration/registration"

    server.stubFor(
      post(urlEqualTo(s"$registrationUrl$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )
  }

}
