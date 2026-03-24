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
import models.FinancialInstitutions.{CreateFIDetails, FIDetail, RemoveFIDetail, SubmitFIDetailsResponse}
import models.error.ApiError.{JsValidationError, UnexpectedResponse}
import models.readFIs.response.ViewFIDetailsResponse
import models.updateFi.CreateFiResponse
import play.api.http.Status.{OK, UNPROCESSABLE_ENTITY}
import play.api.i18n.Lang.logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HttpErrorFunctions.is5xx
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
          val message = s"Unexpected response when retrieving FIs for subscriptionId: $subscriptionId, status: ${res.status} and response: ${res.body}"
          if (is5xx(res.status)) logger.error(message) else logger.warn(message)
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
          val message = s"Unexpected response when retrieving an FI for subscriptionId: $subscriptionId, status: ${res.status} and response: ${res.body}"
          if (is5xx(res.status)) logger.error(message) else logger.warn(message)
          Future.failed(UnexpectedResponse)
      }

  def updateFI(fiDetails: FIDetail)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] =
    (fiDetails match {
      case _: FIDetail => httpClient.put(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/update")
    }).withBody(Json.toJson(fiDetails))
      .execute[HttpResponse]
      .flatMap {
        case res if res.status == OK =>
          Future.successful(())
        case res =>
          val message =
            s"Unexpected response when updating an FI for subscriptionId: ${fiDetails.SubscriptionID}, status: ${res.status} and response: ${res.body}"
          if (is5xx(res.status)) logger.error(message) else logger.warn(message)
          Future.failed(UnexpectedResponse)
      }

  def addFI(fiDetails: CreateFIDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SubmitFIDetailsResponse] =
    (fiDetails match {
      case _: CreateFIDetails => httpClient.post(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/create")
    }).withBody(Json.toJson(fiDetails))
      .execute[HttpResponse]
      .flatMap {
        case res if res.status == OK =>
          res.json.validate[CreateFiResponse] match {
            case JsSuccess(createFiResponse, _) =>
              Future.successful(SubmitFIDetailsResponse(createFiResponse.ResponseDetails.ReturnParameters.Value))
            case JsError(errors) =>
              logger.error(s"Failed to parse create FI response for subscriptionId: ${fiDetails.SubscriptionID}, errors: $errors")
              Future.failed(JsValidationError)
          }
        case res =>
          val message =
            s"Unexpected response when creating an FI for subscriptionId: ${fiDetails.SubscriptionID}, status: ${res.status} and response: ${res.body}"
          if (is5xx(res.status)) logger.error(message) else logger.warn(message)
          Future.failed(UnexpectedResponse)
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
