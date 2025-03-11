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
import models.FinancialInstitutions.{BaseFIDetail, CreateFIDetails, FIDetail, RemoveFIDetail}
import play.api.i18n.Lang.logger
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

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

  def addOrUpdateFI(fiDetails: BaseFIDetail)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    writes: Writes[BaseFIDetail]
  ): Future[HttpResponse] =
    (fiDetails match {
      case _: CreateFIDetails => httpClient.post(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/create")
      case _: FIDetail        => httpClient.put(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/update")
    }).withBody(Json.toJson(fiDetails))
      .execute[HttpResponse]
      .andThen {
        case Failure(exception) => logger.error(s"Failed to add or update FI: ${exception.getMessage}", exception)
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
