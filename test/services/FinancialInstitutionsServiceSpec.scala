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

import base.SpecBase
import connectors.FinancialInstitutionsConnector
import generators.{ModelGenerators, UserAnswersGenerator}
import models.FinancialInstitutions.FIDetail
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import play.api.libs.json.{JsArray, JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsServiceSpec extends SpecBase with ModelGenerators with UserAnswersGenerator with ScalaCheckPropertyChecks {

  val mockConnector: FinancialInstitutionsConnector = mock[FinancialInstitutionsConnector]
  val sut                                           = new FinancialInstitutionsService(mockConnector)

  implicit override val hc: HeaderCarrier = HeaderCarrier()

  "FinancialInstitutionsService" - {

    "getListOfFinancialInstitutions extracts list of FI details" in {
      val subscriptionId = "XE5123456789"
      val mockResponse   = Future.successful(HttpResponse(OK, testViewFIDetailsBody))

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = sut.getListOfFinancialInstitutions(subscriptionId)
      result.futureValue mustBe testFiDetails
    }

    "getFinancialInstitution" - {

      "must return financial institution details" in {
        forAll {
          fiDetail: FIDetail =>
            val viewFIDetailsResponse = createViewFIDetailsResponse(Seq(fiDetail))

            when(mockConnector.viewFi(fiDetail.SubscriptionID, fiDetail.FIID))
              .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(viewFIDetailsResponse), Map.empty)))

            val result = sut.getFinancialInstitution(fiDetail.SubscriptionID, fiDetail.FIID)

            result.futureValue.value mustBe fiDetail
        }
      }

      "must return None when there is no financial institution details" in {
        forAll {
          fiDetail: FIDetail =>
            val viewFIDetailsResponse = createViewFIDetailsResponse(Nil)

            when(mockConnector.viewFi(fiDetail.SubscriptionID, fiDetail.FIID))
              .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(viewFIDetailsResponse), Map.empty)))

            val result = sut.getFinancialInstitution(fiDetail.SubscriptionID, fiDetail.FIID)

            result.futureValue mustBe empty
        }
      }

    }

    "getInstitutionById extracts details matching given FIID" in {
      val fiid      = "683373339"
      val noFiid    = "000000000"
      val fiDetails = testFiDetails

      sut.getInstitutionById(fiDetails, fiid) mustBe Some(testFiDetail)
      sut.getInstitutionById(fiDetails, noFiid) mustBe None

    }
    "addFinancialInstitution" - {
      val subscriptionId = "XE5123456789"

      "adds FI details" in {
        val mockResponse = Future.successful(Right(HttpResponse(OK, "{}")))

        forAll(fiNotRegistered.arbitrary) {
          (userAnswers: UserAnswers) =>
            when(mockConnector.addOrUpdateFI(any(), any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(mockResponse)
            val result = sut.addOrUpdateFinancialInstitution(subscriptionId, userAnswers, "POST")
            result.futureValue mustBe ()
        }
      }
    }

    "removeFinancialInstitution triggers removeFi" in {
      val mockResponse = Future.successful(HttpResponse(OK, "{}"))
      when(mockConnector.removeFi(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(mockResponse)
      val result = sut.removeFinancialInstitution(testFiDetail)
      result.futureValue mustBe ()
    }

  }

  private def createViewFIDetailsResponse(fiDetails: Seq[FIDetail]): JsObject = {
    val fiDetailsJsObject       = Json.obj(("FIDetails", JsArray(fiDetails.map(Json.toJson(_)))))
    val responseDetailsJsObject = Json.obj(("ResponseDetails", fiDetailsJsObject))
    Json.obj(("ViewFIDetails", responseDetailsJsObject))
  }

}
