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

import config.FrontendAppConfig
import models.{FileDetailsResult, IntenalIssueError, UnExpectedResponse}
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.i18n.Lang.logger
import play.api.libs.json.{JsError, JsSuccess}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileDetailsConnector @Inject() (val config: FrontendAppConfig, val httpClient: HttpClientV2) {

  def checkSubscriptionHasRecentSubmissions(subscriptionId: String, page: Int = 1)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url = url"${config.crsFatcaReportingUrl}/crs-fatca-reporting/files/details/$subscriptionId?page=$page"

    httpClient
      .get(url)
      .execute[HttpResponse]
      .flatMap {
        case responseMessage if responseMessage.status == OK =>
          Future.successful(true)
        case responseMessage =>
          logger.warn(s"FileDetailsConnector: Failed to check for recent submissions: ${responseMessage.status} and response: ${responseMessage.body}")
          Future.failed(UnExpectedResponse)
      }
      .recoverWith {
        case _: NotFoundException =>
          logger.warn(s"FileDetailsConnector: No file details found for subscriptionId: $subscriptionId")
          Future.successful(false)
        case e: Exception =>
          logger.error(s"FileDetailsConnector: Exception occurred while checking for recent submissions", e)
          Future.failed(IntenalIssueError)
      }
  }

}
