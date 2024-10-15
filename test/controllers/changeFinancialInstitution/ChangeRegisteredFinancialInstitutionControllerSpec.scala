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

package controllers.changeFinancialInstitution

import base.SpecBase
import controllers.routes
import generators.ModelGenerators
import models.FinancialInstitutions.FIDetail
import models.UserAnswers
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FinancialInstitutionUpdateService, FinancialInstitutionsService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ThereIsAProblemView

import scala.concurrent.{ExecutionContext, Future}

class ChangeRegisteredFinancialInstitutionControllerSpec
    extends SpecBase
    with MockitoSugar
    with ModelGenerators
    with BeforeAndAfterEach
    with ScalaCheckDrivenPropertyChecks {

  private val SubscriptionId                        = "subscriptionId"
  private val SendButtonText                        = "Confirm and send"
  private val mockFinancialInstitutionsService      = mock[FinancialInstitutionsService]
  private val mockFinancialInstitutionUpdateService = mock[FinancialInstitutionUpdateService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFinancialInstitutionsService, mockFinancialInstitutionUpdateService)
  }

  "ChangeRegisteredBusinessFinancialInstitutionController" - {

    "onPageLoad" - {

      "when change FI details is not in progress" - {

        "must return OK and the correct view without the 'Confirm and send' button for a GET when ChangeFiDetailsInProgress is false" in {
          forAll {
            fiDetail: FIDetail =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              mockSuccessfulUserAnswersPersistence(userAnswers, fiDetail)

              val application = createAppWithAnswers(Option(userAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe true
              }
          }
        }

        "must return OK and the correct view without the 'Confirm and send' button for a GET when ChangeFiDetailsInProgress is not set" in {
          forAll {
            fiDetail: FIDetail =>
              val userAnswers = emptyUserAnswers

              mockSuccessfulFiRetrieval(fiDetail)
              mockSuccessfulUserAnswersPersistence(userAnswers, fiDetail)

              val application = createAppWithAnswers(Option(userAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe true
              }
          }
        }

        "must return INTERNAL_SERVER_ERROR when an error occurs during persistence of FI details" in {
          forAll {
            fiDetail: FIDetail =>
              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.populateAndSaveFiDetails(any(), any()))
                .thenReturn(Future.failed(new Exception("failed to populate and save FI details")))

              val application = createAppWithAnswers(Option(emptyUserAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                val view = application.injector.instanceOf[ThereIsAProblemView]

                status(result) mustEqual INTERNAL_SERVER_ERROR
                contentAsString(result) mustEqual view()(request, messages(application)).toString
              }
          }
        }
      }

      "when change FI details is in progress" - {

        "must return OK and the correct view with the 'Confirm and send' button for a GET when FI details has been changed" in {
          forAll {
            fiDetail: FIDetail =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.fiDetailsHasChanged(mockitoEq(userAnswers), mockitoEq(fiDetail)))
                .thenReturn(true)

              val application = createAppWithAnswers(Option(userAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe false
              }
          }
        }

        "must return OK and the correct view without the 'Confirm and send' button for a GET when FI details has NOT been changed" in {
          forAll {
            fiDetail: FIDetail =>
              val userAnswers = emptyUserAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.fiDetailsHasChanged(mockitoEq(userAnswers), mockitoEq(fiDetail)))
                .thenReturn(false)

              val application = createAppWithAnswers(Option(userAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe true
              }
          }
        }
      }

      "must return INTERNAL_SERVER_ERROR when unable to find FI details" in {
        forAll {
          fiDetail: FIDetail =>
            when(mockFinancialInstitutionsService.getFinancialInstitution(any(), any())(any(), any()))
              .thenReturn(Future.successful(None))

            val application = createAppWithAnswers(Option(emptyUserAnswers))
            running(application) {
              val request =
                FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[ThereIsAProblemView]

              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual view()(request, messages(application)).toString
            }
        }
      }

      "must return INTERNAL_SERVER_ERROR when an error occurs during retrieval of FI details" in {
        forAll {
          fiDetail: FIDetail =>
            when(mockFinancialInstitutionsService.getFinancialInstitution(any(), any())(any(), any()))
              .thenReturn(Future.failed(new Exception("failed to read FI details")))

            val application = createAppWithAnswers(Option(emptyUserAnswers))
            running(application) {
              val request =
                FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[ThereIsAProblemView]

              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual view()(request, messages(application)).toString
            }
        }
      }
    }

    "confirmAndAdd" - {
      "must clear user answers data and redirect to JourneyRecovery for a POST" in {
        val userAnswers = emptyUserAnswers

        when(mockFinancialInstitutionUpdateService.clearUserAnswers(any[UserAnswers])).thenReturn(Future.successful(true))

        val application = createAppWithAnswers(Option(userAnswers))
        running(application) {
          val request = FakeRequest(POST, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          verify(mockFinancialInstitutionUpdateService, times(1)).clearUserAnswers(userAnswers)
        }
      }

      "must return INTERNAL_SERVER_ERROR for a POST when an error occurs when clearing user answers" in {
        when(mockFinancialInstitutionUpdateService.clearUserAnswers(any[UserAnswers]))
          .thenReturn(Future.failed(new Exception("failed to clear user answers data")))

        val application = createAppWithAnswers(Option(emptyUserAnswers))
        running(application) {
          val request = FakeRequest(POST, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.confirmAndAdd().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ThereIsAProblemView]

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }
    }
  }

  private def createAppWithAnswers(userAnswers: Option[UserAnswers]) =
    applicationBuilder(userAnswers)
      .overrides(
        bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
        bind[FinancialInstitutionUpdateService].toInstance(mockFinancialInstitutionUpdateService)
      )
      .build()

  private def mockSuccessfulFiRetrieval(fiDetail: FIDetail) =
    when(
      mockFinancialInstitutionsService
        .getFinancialInstitution(mockitoEq(SubscriptionId), mockitoEq(fiDetail.FIID))(any[HeaderCarrier], any[ExecutionContext])
    ).thenReturn(Future.successful(Option(fiDetail)))

  private def mockSuccessfulUserAnswersPersistence(userAnswers: UserAnswers, fiDetail: FIDetail) =
    when(
      mockFinancialInstitutionUpdateService.populateAndSaveFiDetails(mockitoEq(userAnswers), mockitoEq(fiDetail))
    ).thenReturn(Future.successful(userAnswers))

}
