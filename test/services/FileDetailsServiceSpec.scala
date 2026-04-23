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

package services

import base.SpecBase
import connectors.FileDetailsConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class FileDetailsServiceSpec extends SpecBase {
  val mockFileDetailsConnector = mock[FileDetailsConnector]

  "FileDetailsService" - {
    "checkSubscriptionHasRecentSubmissions should return true" in {
      when(mockFileDetailsConnector.checkSubscriptionHasRecentSubmissions(any[String], any[Int]())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val service = new FileDetailsService(mockFileDetailsConnector)
      val result  = service.checkSubscriptionHasRecentSubmissions("test-subscription-id")(HeaderCarrier(), ec = ExecutionContext.global)

      result.futureValue mustBe true
    }

    "checkSubscriptionHasRecentSubmissions should return false if an exception is thrown" in {
      when(mockFileDetailsConnector.checkSubscriptionHasRecentSubmissions(any[String], any[Int]())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Test exception")))

      val service = new FileDetailsService(mockFileDetailsConnector)
      val result  = service.checkSubscriptionHasRecentSubmissions("test-subscription-id")(HeaderCarrier(), ec = ExecutionContext.global)

      result.futureValue mustBe false
    }
  }

}
