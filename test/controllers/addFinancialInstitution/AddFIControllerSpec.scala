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
import org.mockito.Mockito.when
import org.scalatest.PrivateMethodTester
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.{Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

class AddFIControllerSpec extends SpecBase with PrivateMethodTester {

  "Add FI Controller" - {

    "if has CT UTR" - {
      "must redirect to report for registered business page" in {
        val mockCtUtrRetrievalAction: CtUtrRetrievalAction = mock[CtUtrRetrievalAction]
        when(mockCtUtrRetrievalAction.apply()).thenReturn(new FakeCtUtrRetrievalAction())

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CtUtrRetrievalAction].toInstance(mockCtUtrRetrievalAction)
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
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

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
      val retrieveCtUTR                                      = mock[CtUtrRetrievalAction]

      val sut = new AddFIController(controllerComponents, identify, retrieveCtUTR)

      val privateRedirectUrl = PrivateMethod[Call](Symbol("redirectUrl"))

      "returns /report-for-registered-business when Org is autoMatched" in {
        val result = sut.invokePrivate(privateRedirectUrl(true, Organisation))

        result.url mustBe controllers.addFinancialInstitution.registeredBusiness.routes.ReportForRegisteredBusinessController.onPageLoad(NormalMode).url
      }
      "returns /name when Individual is autoMatched" in {
        val result = sut.invokePrivate(privateRedirectUrl(true, Individual))

        result.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
      }
      "returns /name when not autoMatched and Org" in {
        val orgResult = sut.invokePrivate(privateRedirectUrl(false, Organisation))

        orgResult.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
      }
      "returns /name when not autoMatched and Ind" in {
        val indResult = sut.invokePrivate(privateRedirectUrl(false, Individual))

        indResult.url mustBe routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode).url
      }
    }
  }

}
