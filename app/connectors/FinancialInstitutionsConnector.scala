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

import config.FrontendAppConfig
import models.FinancialInstitutions.{CreateFIDetails, RemoveFIDetail}
import models.response.ErrorDetails
import play.api.http.Status.OK
import play.api.i18n.Lang.logger
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsConnector @Inject() (val config: FrontendAppConfig, val httpClient: HttpClientV2) {

  def viewFis(subscriptionId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId")
      .execute[HttpResponse]

  def viewFi(subscriptionId: String, fiId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId")
      .execute[HttpResponse]

  def addOrUpdateFI(fiDetails: CreateFIDetails, requestType: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ErrorDetails, HttpResponse]] =
    if (requestType == "POST") {
      httpClient
        .post(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/create")
        .withBody(Json.toJson(fiDetails))
        .execute[HttpResponse]
        .map {
          response =>
            response.status match {
              case OK => Right(response)
              case _  => Left((response.json \ "ErrorDetails").as[ErrorDetails])
            }
        }
        .recoverWith {
          case e: Exception =>
            logger.error(s"Error while adding an FI: ${e.getMessage}")
            Future.successful(Left(ErrorDetails(s"${LocalDateTime.now()}", fiDetails.SubscriptionID, None, Some(s"Add FI failed: ${e.getMessage}"))))
        }
    } else {
      httpClient
        .put(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/create")
        .withBody(Json.toJson(fiDetails))
        .execute[HttpResponse]
        .map {
          response =>
            response.status match {
              case OK => Right(response)
              case _  => Left((response.json \ "ErrorDetails").as[ErrorDetails])
            }
        }
        .recoverWith {
          case e: Exception =>
            logger.error(s"Error while updating an FI: ${e.getMessage}")
            Future.successful(Left(ErrorDetails(s"${LocalDateTime.now()}", fiDetails.SubscriptionID, None, Some(s"Update FI failed: ${e.getMessage}"))))
        }
    }

  def removeFi(fiDetails: RemoveFIDetail)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .post(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/remove")
      .withBody(Json.toJson(fiDetails))
      .execute[HttpResponse]

}
