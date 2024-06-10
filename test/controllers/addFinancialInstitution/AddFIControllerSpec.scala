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
import controllers.actions.{CtUtrRetrievalAction, FakeCtUtrRetrievalAction}
import models.NormalMode
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AddFIControllerSpec extends SpecBase {

  "Add FI Controller" - {

    "if has CT UTR" - {
      "must redirect to name of report for registered business page" in {
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

  }

}
