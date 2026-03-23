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
import models.FinancialInstitutions.{FIDetail, SubmitFIDetailsResponse}
import models.UserAnswers
import models.error.ApiError.{NoMatchingRecords, UnexpectedResponse}
import models.readFIs.response.ViewFIDetailsResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.http.Status.OK
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsServiceSpec extends SpecBase with ModelGenerators with UserAnswersGenerator with ScalaCheckPropertyChecks {

  val mockConnector: FinancialInstitutionsConnector = mock[FinancialInstitutionsConnector]
  val sut                                           = new FinancialInstitutionsService(mockConnector)

  implicit override val hc: HeaderCarrier = HeaderCarrier()

  "FinancialInstitutionsService" - {

    "getListOfFinancialInstitutions extracts list of FI details" in {
      val subscriptionId        = "XE5123456789"
      val viewFIDetailsResponse = Json.parse(testViewFIDetailsBody).as[ViewFIDetailsResponse]
      val mockResponse          = Future.successful(viewFIDetailsResponse.ViewFIDetails.ResponseDetails.FIDetails)

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = sut.getListOfFinancialInstitutions(subscriptionId)
      result.futureValue mustBe testFiDetails
    }

    "getListOfFinancialInstitutions return empty response when no matching records error is returned" in {
      val subscriptionId = "XE5123456789"
      val mockResponse   = Future.successful(Seq.empty)

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = sut.getListOfFinancialInstitutions(subscriptionId)
      result.futureValue mustBe Seq.empty
    }

    "getListOfFinancialInstitutions throws exception when unexpected error is returned" in {
      val subscriptionId = "XE5123456789"
      val mockResponse   = Future.failed(UnexpectedResponse)

      when(mockConnector.viewFis(subscriptionId)).thenReturn(mockResponse)
      val result: Future[Seq[FIDetail]] = sut.getListOfFinancialInstitutions(subscriptionId)
      an[Exception] must be thrownBy result.futureValue
    }

    "getFinancialInstitution" - {
      "must return financial institution details" in {
        val fiId                  = "some-fiid"
        val subscriptionId        = "XE5123456789"
        val viewFIDetailsResponse = Json.parse(testViewFIDetailsBody).as[ViewFIDetailsResponse]
        val mockResponse          = Future.successful(viewFIDetailsResponse.ViewFIDetails.ResponseDetails.FIDetails.headOption)

        when(mockConnector.viewFi(subscriptionId, fiId))
          .thenReturn(mockResponse)

        val result = sut.getFinancialInstitution(subscriptionId, fiId)

        result.futureValue.value mustBe testFiDetail

      }

      "must return None when there is no financial institution details" in {
        val fiId           = "some-fiid"
        val subscriptionId = "XE5123456789"
        val mockResponse   = Future.successful(None)

        when(mockConnector.viewFi(subscriptionId, fiId))
          .thenReturn(mockResponse)

        val result = sut.getFinancialInstitution(subscriptionId, fiId)

        result.futureValue mustBe None
      }

      "throws exception when unexpected error is returned" in {
        val fiId           = "some-fiid"
        val subscriptionId = "XE5123456789"
        val mockResponse   = Future.failed(UnexpectedResponse)

        when(mockConnector.viewFi(subscriptionId, fiId))
          .thenReturn(mockResponse)

        val result = sut.getFinancialInstitution(subscriptionId, fiId)
        an[Exception] must be thrownBy result.futureValue
      }
    }

    "getInstitutionById extracts details matching given FIID" in {
      val fiid      = "683373339"
      val noFiid    = "000000000"
      val fiDetails = testFiDetails

      sut.getInstitutionById(fiDetails, fiid) mustBe Some(testFiDetail)
      sut.getInstitutionById(fiDetails, noFiid) mustBe None

    }

    "addFinancialInstitution adds FI details" in {
      val subscriptionId = "XE5123456789"

      val responseJson =
        s"""
            |{
            |  "ResponseDetails": {
            |    "ReturnParameters": {
            |      "Value": "$testFiid"
            |    }
            |  }
            |}
            |""".stripMargin
      val mockResponse = Future.successful(HttpResponse(OK, responseJson))

      forAll(fiNotRegistered.arbitrary) {
        (userAnswers: UserAnswers) =>
          when(mockConnector.addFI(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(mockResponse)
          val result = sut.addFinancialInstitution(subscriptionId, userAnswers)
          result.futureValue mustBe SubmitFIDetailsResponse(Some(testFiid))
      }
    }

    "updateFinancialInstitution updates FI details" in {
      val subscriptionId = "XE5123456789"

      val mockResponse = Future.successful(HttpResponse(OK, "{}"))

      forAll(fiRegistered.arbitrary) {
        (userAnswers: UserAnswers) =>
          val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, "123456789")
          when(mockConnector.updateFI(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(mockResponse)
          val result = sut.updateFinancialInstitution(subscriptionId, updatedAnswers)
          result.futureValue mustBe ()
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
