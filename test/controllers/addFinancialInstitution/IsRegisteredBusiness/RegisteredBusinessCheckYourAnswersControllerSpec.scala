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

package controllers.addFinancialInstitution.IsRegisteredBusiness

import base.SpecBase
import controllers.routes
import generators.{ModelGenerators, UserAnswersGenerator}
import models.FinancialInstitutions.SubmitFIDetailsResponse
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FinancialInstitutionsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

class RegisteredBusinessCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with ModelGenerators with UserAnswersGenerator {

  val list: SummaryList                                              = SummaryListViewModel(Seq.empty)
  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]

  "RegisteredBusinessCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {
      def onwardRoute: Call = Call("GET", "/foo")

      forAll(fiRegistered.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Option(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
            )
            .build()

          running(application) {
            val request =
              FakeRequest(GET, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual OK
          }
      }
    }

    "must redirect to information-sent page for a GET when the user answers data is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad.url
      }
    }

    "redirect to Missing Information when missing some UserAnswers for individual with id" in {
      def onwardRoute: Call = Call("GET", "/foo")

      forAll(fiRegisteredMissingAnswers.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Option(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
            )
            .build()

          running(application) {
            val request =
              FakeRequest(GET, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe routes.SomeInformationMissingController.onPageLoad().url
          }
      }
    }

    "confirmAndAdd" - {
      "must redirect to financial-institution-added when create fi call is successful" in {
        when(mockFinancialInstitutionsService.addFinancialInstitution(any(), any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(SubmitFIDetailsResponse(Some(testFiid))))
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(data = Json.obj(("key", "value")))))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.addFinancialInstitution.routes.FinancialInstitutionAddedConfirmationController.onPageLoad.url

        }
      }

      "must redirect to information-sent page for a GET when the user answers data is empty" in {
        val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad.url
        }
      }
    }
  }

}
