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
import models.FinancialInstitutions.{CreateFIDetails, FIDetail, RemoveFIDetail}
import models.error.ApiError.{JsValidationError, UnexpectedResponse}
import models.readFIs.response.ViewFIDetailsResponse
import play.api.http.Status.{OK, UNPROCESSABLE_ENTITY}
import play.api.i18n.Lang.logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class FinancialInstitutionsConnector @Inject() (val config: FrontendAppConfig, val httpClient: HttpClientV2) {

  def viewFis(subscriptionId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[FIDetail]] =
    httpClient
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId")
      .execute[HttpResponse]
      .flatMap {
        case res if res.status == OK =>
          res.json.validate[ViewFIDetailsResponse] match {
            case JsSuccess(viewFiDetails, _) => Future.successful(viewFiDetails.ViewFIDetails.ResponseDetails.FIDetails)
            case JsError(errors) =>
              logger.error(s"Failed to parse FIs for subscriptionId: $subscriptionId, errors: $errors")
              Future.failed(JsValidationError)
          }
        case res if res.status == UNPROCESSABLE_ENTITY && (Json.parse(res.body) \ "errorDetail" \ "errorCode").as[String] == "001" =>
          logger.warn(s"No FIs found for subscriptionId: $subscriptionId")
          Future.successful(Seq.empty)
        case res =>
          logger.error(s"Unexpected response when retrieving FIs for subscriptionId: $subscriptionId, response: ${res.body} and status: ${res.status}")
          Future.failed(UnexpectedResponse)
      }

  def viewFi(subscriptionId: String, fiId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[FIDetail]] =
    httpClient
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId")
      .execute[HttpResponse]
      .flatMap {
        case res if res.status == OK =>
          res.json.validate[ViewFIDetailsResponse] match {
            case JsSuccess(viewFiDetails, _) => Future.successful(viewFiDetails.ViewFIDetails.ResponseDetails.FIDetails.headOption)
            case JsError(errors) =>
              logger.error(s"Failed to parse an FI for subscriptionId: $subscriptionId errors: $errors")
              Future.failed(JsValidationError)
          }
        case res if res.status == UNPROCESSABLE_ENTITY && (Json.parse(res.body) \ "errorDetail" \ "errorCode").as[String] == "001" =>
          logger.warn(s"No FI found for subscriptionId: $subscriptionId")
          Future.successful(None)
        case res =>
          logger.error(s"Unexpected response when retrieving an FI for subscriptionId: $subscriptionId, response: ${res.body} and status: ${res.status}")
          Future.failed(UnexpectedResponse)
      }

  def updateFI(fiDetails: FIDetail)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    (fiDetails match {
      case _: FIDetail => httpClient.put(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/update")
    }).withBody(Json.toJson(fiDetails))
      .execute[HttpResponse]
      .andThen {
        case Failure(exception) => logger.error(s"Failed to update FI: ${exception.getMessage}", exception)
      }

  def addFI(fiDetails: CreateFIDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    (fiDetails match {
      case _: CreateFIDetails => httpClient.post(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/create")
    }).withBody(Json.toJson(fiDetails))
      .execute[HttpResponse]
      .andThen {
        case Failure(exception) => logger.error(s"Failed to add FI: ${exception.getMessage}", exception)
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
