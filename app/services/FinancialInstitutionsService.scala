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

package services

import connectors.FinancialInstitutionsConnector
import models.FinancialInstitutions.FIDetail
import play.api.libs.json.{JsResult, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsService @Inject() (connector: FinancialInstitutionsConnector) {

  def getListOfFinancialInstitutions(subscriptionId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[FIDetail]] =
    connector
      .viewFis(subscriptionId)
      .map(
        res => extractList(res.body)
      )

  private def extractList(body: String) = {
    val json: JsValue                        = Json.parse(body)
    val listsResult: JsResult[Seq[FIDetail]] = (json \ "ViewFIDetails" \ "ResponseDetails" \ "FIDetails").validate[Seq[FIDetail]]
    listsResult.get
  }

}
