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

package models.response

import play.api.libs.json.{Json, OFormat}

case class ErrorDetails(
  timestamp: String,
  correlationId: String,
  errorCode: Option[String] = None,
  errorMessage: Option[String] = None,
  source: Option[String] = None,
  sourceFaultDetail: Option[SourceFault] = None
)

case class SourceFault(
  RestFault: Option[String] = None,
  SoapFault: Option[String] = None,
  Detail: List[String]
)

object SourceFault {
  implicit val sourceFaultDetailReads: OFormat[SourceFault] = Json.format[SourceFault]
}

object ErrorDetails {
  implicit val errorDetailReads: OFormat[ErrorDetails] = Json.format[ErrorDetails]
}

case class DownstreamServiceError(message: String, cause: Throwable) extends RuntimeException
