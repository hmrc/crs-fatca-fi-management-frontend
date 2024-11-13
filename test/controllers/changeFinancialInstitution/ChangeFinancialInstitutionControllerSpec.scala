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
import generators.{ModelGenerators, UserAnswersGenerator}
import models.FinancialInstitutions.FIDetail
import models.{RequestType, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FinancialInstitutionUpdateService, FinancialInstitutionsService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ThereIsAProblemView

import scala.concurrent.{ExecutionContext, Future}

class ChangeFinancialInstitutionControllerSpec
    extends SpecBase
    with MockitoSugar
    with ModelGenerators
    with BeforeAndAfterEach
    with UserAnswersGenerator
    with ScalaCheckDrivenPropertyChecks {

  private val SubscriptionId                        = "subscriptionId"
  private val SendButtonText                        = "Confirm and send"
  private val mockFinancialInstitutionsService      = mock[FinancialInstitutionsService]
  private val mockFinancialInstitutionUpdateService = mock[FinancialInstitutionUpdateService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFinancialInstitutionsService, mockFinancialInstitutionUpdateService)
  }

  "ChangeFinancialInstitutionController" - {

    "onPageLoad" - {

      "when change FI details is not in progress" - {

        "must return OK and the correct view without the 'Confirm and send' button for a GET when ChangeFiDetailsInProgress is false" in {
          forAll(fiNotRegistered.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              mockSuccessfulUserAnswersPersistence(updatedAnswers, fiDetail)

              val application = createAppWithAnswers(Option(updatedAnswers))
              running(application) {
                val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

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
                val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

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
                val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

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
          forAll(fiNotRegistered.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.fiDetailsHasChanged(mockitoEq(updatedAnswers), mockitoEq(fiDetail)))
                .thenReturn(true)

              val application = createAppWithAnswers(Option(updatedAnswers))
              running(application) {
                val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe false
              }
          }
        }

        "must return OK and the correct view without the 'Confirm and send' button for a GET when FI details has NOT been changed" in {
          forAll(fiNotRegistered.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.fiDetailsHasChanged(mockitoEq(updatedAnswers), mockitoEq(fiDetail)))
                .thenReturn(false)

              val application = createAppWithAnswers(Option(updatedAnswers))
              running(application) {
                val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe true
              }
          }
        }

        "must redirect to information missing page when ChangeFiDetails In Progress and missing answers" in {
          forAll(fiNotRegisteredMissingAnswers.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.fiDetailsHasChanged(mockitoEq(updatedAnswers), mockitoEq(fiDetail)))
                .thenReturn(false)

              val application = createAppWithAnswers(Option(updatedAnswers))
              running(application) {
                val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual routes.SomeInformationMissingController.onPageLoad().url
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
              val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

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
              val request = FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[ThereIsAProblemView]

              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual view()(request, messages(application)).toString
            }
        }
      }
    }

    "confirmAndAdd" - {
      val mockService = mock[FinancialInstitutionsService]

      val someUserAnswers = emptyUserAnswers
        .withPage(NameOfFinancialInstitutionPage, "test")
        .withPage(HaveUniqueTaxpayerReferencePage, false)
        .withPage(HaveGIINPage, false)
        .withPage(WhereIsFIBasedPage, false)
        .withPage(WhereIsFIBasedPage, true)
        .withPage(SelectedAddressLookupPage, testAddressLookup)
        .withPage(IsThisAddressPage, true)
        .withPage(FirstContactNamePage, "MrTest")
        .withPage(FirstContactEmailPage, "MrTest@test.com")
        .withPage(FirstContactHavePhonePage, false)
        .withPage(SecondContactExistsPage, false)

      "must redirect to error page when an exception is thrown" in {

        when(mockService.addOrUpdateFinancialInstitution(any[String](), any[UserAnswers](), any[RequestType]())(any[HeaderCarrier](), any[ExecutionContext]()))
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
      "must redirect to details updated page when submitting answers" in {
        when(mockService.addOrUpdateFinancialInstitution(any[String](), any[UserAnswers](), any[RequestType]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful())

        val application = applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.DetailsUpdatedController.onPageLoad().url
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
