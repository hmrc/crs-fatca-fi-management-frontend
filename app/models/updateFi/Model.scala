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

package models.updateFi

import play.api.libs.json.{Json, OFormat}

case class CreateFiReturnParameters(Key: String, Value: String)

object CreateFiReturnParameters {
  implicit val format: OFormat[CreateFiReturnParameters] = Json.format[CreateFiReturnParameters]
}

case class CreateFIResponseDetail(ReturnParameters: CreateFiReturnParameters, processingDate: String)

object CreateFIResponseDetail {
  implicit val format: OFormat[CreateFIResponseDetail] = Json.format[CreateFIResponseDetail]
}

case class CreateFiResponse(ResponseDetails: CreateFIResponseDetail)

object CreateFiResponse {
  implicit val format: OFormat[CreateFiResponse] = Json.format[CreateFiResponse]
}
