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
    "getInstitutionById extracts details matching given FIID" in {
      val fiid      = "683373339"
      val noFiid    = "000000000"
      val fiDetails = testFiDetails

      sut.getInstitutionById(fiDetails, fiid) mustBe Some(testFiDetail)
      sut.getInstitutionById(fiDetails, noFiid) mustBe None

    }
    "addFinancialInstitution adds FI details" in {
      val mockResponse   = Future.successful(HttpResponse(OK, "{}"))
      val subscriptionId = "XE5123456789"
      forAll(fiNotRegistered.arbitrary) {
        (userAnswers: UserAnswers) =>
          when(mockConnector.addFi(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(mockResponse)
          val result = sut.addFinancialInstitution(subscriptionId, userAnswers)
          result.futureValue mustBe ()
      }

    }

  }

}
