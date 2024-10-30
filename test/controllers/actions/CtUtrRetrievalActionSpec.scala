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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.requests.IdentifierRequest
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}

import scala.concurrent.{ExecutionContext, Future}

class CtUtrRetrievalActionSpec extends SpecBase with MockitoSugar {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val ctUtrEnrolment                   = Set(Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", "1234567890")), "Activated"))
  val orgRequest                       = IdentifierRequest(FakeRequest(), "FATCAID", "IR-CT", Organisation, ctUtrEnrolment)

  "Ct Utr Retrieval Action" - {
    "where is valid utr" - {
      "should return automatched as true when affinity group not agent" in {
        val ctUtrRetrievalAction = new CtUtrRetrievalActionProvider(mockAppConfig)
        when(mockAppConfig.ctEnrolmentKey) thenReturn "IR-CT"

        val result = ctUtrRetrievalAction.invokeBlock(orgRequest, (req: IdentifierRequest[_]) => Future.successful(Ok(s"${req.autoMatched}")))

        status(result) mustBe OK
        contentAsString(result) mustBe "true"
      }

      "should return automatched as false when affinity group is agent" in {
        val ctUtrRetrievalAction = new CtUtrRetrievalActionProvider(mockAppConfig)
        when(mockAppConfig.ctEnrolmentKey) thenReturn "IR-CT"

        val agentRequest = orgRequest.copy(userType = Agent)

        val result = ctUtrRetrievalAction.invokeBlock(agentRequest, (req: IdentifierRequest[_]) => Future.successful(Ok(s"${req.autoMatched}")))

        status(result) mustBe OK
        contentAsString(result) mustBe "false"
      }
    }
  }

}
