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
import controllers.routes
import generators.{ModelGenerators, UserAnswersGenerator}
import models.FinancialInstitutions.SubmitFIDetailsResponse
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.addFinancialInstitution._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FinancialInstitutionsService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with ModelGenerators with UserAnswersGenerator {
  implicit val mockMessages: Messages = mock[Messages]

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {
        def onwardRoute: Call = Call("GET", "/foo")

        forAll(fiNotRegistered.arbitrary) {
          (userAnswers: UserAnswers) =>
            val application = applicationBuilder(userAnswers = Option(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
              )
              .build()

            running(application) {
              val request = FakeRequest(GET, controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad().url)

              val result = route(application, request).value

              status(result) mustEqual OK
            }
        }
      }
    }

    "must redirect to information-sent page for a GET when the user answers is empty" in {
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.InformationSentController.onPageLoad.url
      }
    }

    "redirect to Missing Information when missing some UserAnswers for individual with id" in {
      def onwardRoute: Call = Call("GET", "/foo")

      forAll(fiNotRegisteredMissingAnswers.arbitrary) {
        (userAnswers: UserAnswers) =>
          val application = applicationBuilder(userAnswers = Option(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe routes.SomeInformationMissingController.onPageLoad().url
          }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.addFinancialInstitution.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "confirmAndAdd" - {
      val mockService = mock[FinancialInstitutionsService]

      val someUserAnswers = emptyUserAnswers
        .withPage(NameOfFinancialInstitutionPage, "test")
        .withPage(HaveGIINPage, false)
        .withPage(SelectedAddressLookupPage, testAddressLookup)
        .withPage(IsThisAddressPage, true)
        .withPage(FirstContactNamePage, "MrTest")
        .withPage(FirstContactEmailPage, "MrTest@test.com")
        .withPage(FirstContactHavePhonePage, false)
        .withPage(SecondContactExistsPage, false)

      "must redirect to error page when an exception is thrown" in {

        when(mockService.addFinancialInstitution(any[String](), any[UserAnswers]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.failed(new Exception("Something went wrong")))

        val application = applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.addFinancialInstitution.routes.CheckYourAnswersController.confirmAndAdd().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "must redirect to confirmation page when submitting answers" in {
        when(mockService.addFinancialInstitution(any[String](), any[UserAnswers]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(SubmitFIDetailsResponse(Some(testFiid))))

        val application = applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.addFinancialInstitution.routes.CheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.addFinancialInstitution.routes.FinancialInstitutionAddedConfirmationController.onPageLoad.url
        }
      }

      "must redirect to information-sent page for a POST when the user answers is empty" in {
        val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, controllers.addFinancialInstitution.routes.CheckYourAnswersController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.InformationSentController.onPageLoad.url
        }
      }
    }
  }

}
