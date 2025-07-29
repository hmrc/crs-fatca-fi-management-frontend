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

package controllers.addFinancialInstitution

import base.SpecBase
import controllers.actions.{CtUtrRetrievalAction, FakeCtUtrRetrievalAction, IdentifierAction}
import models.NormalMode
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.PrivateMethodTester
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.{Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AddFIControllerSpec extends SpecBase with PrivateMethodTester with ScalaFutures {

  "Add FI Controller" - {
    val mockCtUtrRetrievalAction: CtUtrRetrievalAction = mock[CtUtrRetrievalAction]
    val mockSessionRepository                          = mock[SessionRepository]

    when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
    when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction())
    val mockFinancialInstitutionsService = mock[FinancialInstitutionsService]
    when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(Seq.empty))

    "if has CT UTR" - {
      "must redirect to report for registered business page" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CtUtrRetrievalAction].toInstance(mockCtUtrRetrievalAction),
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.AddFIController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode).url
          )
        }
      }
    }

    "if does not have CT UTR" - {
      "must redirect to name of financial institution page" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
          )
          .build()
        running(application) {
          val request = FakeRequest(GET, routes.AddFIController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url)
        }
      }
    }

    "redirectUrl" - {
      val controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()
      val identify                                           = mock[IdentifierAction]
      val mockCtUtrRetrievalAction                           = mock[CtUtrRetrievalAction]
      val mockFinancialInstitutionsService                   = mock[FinancialInstitutionsService]
      val request: IdentifierRequest[_]                      = IdentifierRequest(FakeRequest(), "userId", "fatcaId", Individual, autoMatched = true)
      implicit val ec: ExecutionContext                      = scala.concurrent.ExecutionContext.global

      when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction())
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Seq.empty))

      val sut = new AddFIController(controllerComponents, identify, mockCtUtrRetrievalAction, mockFinancialInstitutionsService, mockSessionRepository)(ec)
      val privateRedirectUrl = PrivateMethod[Future[Call]](Symbol("redirectUrl"))

      "when user has already added fis" - {
        "returns /report-for-registered-business when Org is autoMatched" in {
          val result = sut.invokePrivate(privateRedirectUrl(true, request))

          whenReady(result) {
            call =>
              call.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
          }
        }
      }
      "when user does not already have fis added" - {
        "returns /report-for-registered-business when Org is autoMatched" in {
          val result = sut.invokePrivate(privateRedirectUrl(false, request.copy(autoMatched = true, userType = Organisation)))

          whenReady(result) {
            call =>
              call.url mustBe controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode).url
          }
        }
        "returns /name when Individual is autoMatched" in {
          val result = sut.invokePrivate(privateRedirectUrl(false, request.copy(autoMatched = true, userType = Individual)))

          whenReady(result) {
            call =>
              call.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
          }
        }
        "returns /name when not autoMatched and Org" in {
          val orgResult = sut.invokePrivate(privateRedirectUrl(false, request.copy(autoMatched = false, userType = Organisation)))

          whenReady(orgResult) {
            call =>
              call.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
          }
        }
        "returns /name when not autoMatched and Ind" in {
          val indResult = sut.invokePrivate(privateRedirectUrl(false, request.copy(autoMatched = false, userType = Individual)))

          whenReady(indResult) {
            call =>
              call.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
          }
        }
      }
    }
  }

}
