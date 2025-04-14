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
import models.{Address, Country, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution.{HaveGIINPage, UkAddressPage}
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
    with UserAnswersGenerator
    with BeforeAndAfterEach
    with ScalaCheckDrivenPropertyChecks {

  private val SubscriptionId                        = "subscriptionId"
  private val SendButtonText                        = "Confirm and send"
  private val pTagContent                           = " is the business you registered as."
  private val changeRegisteredBusiness              = "Is this financial institution the business you registered as?"
  private val mockFinancialInstitutionsService      = mock[FinancialInstitutionsService]
  private val mockFinancialInstitutionUpdateService = mock[FinancialInstitutionUpdateService]
  private val address: Address                      = Address("value 1", Some("value 2"), "value 3", Some("value 4"), Some("XX9 9XX"), Country.GB)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFinancialInstitutionsService, mockFinancialInstitutionUpdateService)
  }

  "ChangeRegisteredBusinessFinancialInstitutionController" - {

    "onPageLoad" - {

      "when change FI details is not in progress" - {

        "must return OK and the correct view without the 'Confirm and send' button for a GET when ChangeFiDetailsInProgress is false" in {
          forAll(fiRegistered.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              mockSuccessfulUserAnswersPersistence(updatedAnswers, fiDetail)

              val application = createAppWithAnswers(Option(updatedAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

                val result = route(application, request).value

                status(result) mustEqual OK
                val document = Jsoup.parse(contentAsString(result))
                document.getElementsContainingText(SendButtonText).isEmpty mustBe true
                document.getElementsContainingText(pTagContent).isEmpty mustBe true
                document.getElementsContainingText(changeRegisteredBusiness).isEmpty mustBe false
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

        "must navigate To Standard FI When ReportForRegistered Business is set to false" in {
          val fiDetail = testFiDetail
          val userAnswers = emptyUserAnswers
            .withPage(ChangeFiDetailsInProgressId, "12345678")
            .withPage(ReportForRegisteredBusinessPage, false)
            .withPage(UkAddressPage, address)
            .withPage(IsTheAddressCorrectPage, true)
            .withPage(IsThisYourBusinessNamePage, true)

          mockSuccessfulFiRetrieval(fiDetail)
          when(
            mockFinancialInstitutionUpdateService.populateAndSaveRegisteredFiDetails(any(), any())
          ).thenReturn(Future.successful((userAnswers, true)))

          val application = createAppWithAnswers(Option(userAnswers))
          running(application) {
            val request =
              FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController
              .onPageLoad(fiDetail.FIID)
              .url
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
          forAll(fiRegistered.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.registeredFiDetailsHasChanged(mockitoEq(updatedAnswers), mockitoEq(fiDetail)))
                .thenReturn(true)

              val application = createAppWithAnswers(Option(updatedAnswers))
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
          forAll(fiRegistered.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.registeredFiDetailsHasChanged(mockitoEq(updatedAnswers), mockitoEq(fiDetail)))
                .thenReturn(false)

              val application = createAppWithAnswers(Option(updatedAnswers))
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

        "must redirect to information missing page when ChangeFiDetails In Progress and missing answers" in {
          forAll(fiRegisteredMissingAnswers.arbitrary, arbitraryFIDetail.arbitrary) {
            (userAnswers: UserAnswers, fiDetail: FIDetail) =>
              val updatedAnswers = userAnswers.withPage(ChangeFiDetailsInProgressId, fiDetail.FIID)

              mockSuccessfulFiRetrieval(fiDetail)
              when(mockFinancialInstitutionUpdateService.registeredFiDetailsHasChanged(mockitoEq(updatedAnswers), mockitoEq(fiDetail)))
                .thenReturn(false)

              val application = createAppWithAnswers(Option(updatedAnswers))
              running(application) {
                val request =
                  FakeRequest(GET, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(fiDetail.FIID).url)

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

      "must redirect to SomeInformationMissing Page when There is missing values(HaveGIINPage) for request" in {
        val userAnswers = emptyUserAnswers
        val updatedAnswers = userAnswers
          .withPage(ChangeFiDetailsInProgressId, "12345678")
          .withPage(ReportForRegisteredBusinessPage, true)
          .withPage(UkAddressPage, address)
          .withPage(IsTheAddressCorrectPage, true)
          .withPage(IsThisYourBusinessNamePage, true)

        when(mockFinancialInstitutionUpdateService.clearUserAnswers(any[UserAnswers])).thenReturn(Future.successful(true))
        when(
          mockFinancialInstitutionsService.updateFinancialInstitution(any[String], any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful())

        val application = createAppWithAnswers(Option(updatedAnswers))
        running(application) {
          val request = FakeRequest(POST, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SomeInformationMissingController.onPageLoad().url

          verify(mockFinancialInstitutionUpdateService, times(0)).clearUserAnswers(any())
          verify(mockFinancialInstitutionsService, times(0)).updateFinancialInstitution(any(), any())(any(), any())
        }
      }

      "must clear user answers data and redirect to details submitted for a POST" in {
        val userAnswers = emptyUserAnswers
        val updatedAnswers = userAnswers
          .withPage(ChangeFiDetailsInProgressId, "12345678")
          .withPage(ReportForRegisteredBusinessPage, true)
          .withPage(HaveGIINPage, false)
          .withPage(UkAddressPage, address)
          .withPage(IsTheAddressCorrectPage, true)
          .withPage(IsThisYourBusinessNamePage, true)

        when(mockFinancialInstitutionUpdateService.clearUserAnswers(any[UserAnswers])).thenReturn(Future.successful(true))
        when(
          mockFinancialInstitutionsService.updateFinancialInstitution(any[String], any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful())

        val application = createAppWithAnswers(Option(updatedAnswers))
        running(application) {
          val request = FakeRequest(POST, controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.confirmAndAdd().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.DetailsUpdatedController.onPageLoad().url

          verify(mockFinancialInstitutionUpdateService, times(1)).clearUserAnswers(updatedAnswers)
        }
      }

      "must return INTERNAL_SERVER_ERROR for a POST when an error occurs when clearing user answers" in {
        val userAnswers = emptyUserAnswers
        val updatedAnswers = userAnswers
          .withPage(ChangeFiDetailsInProgressId, "12345678")
          .withPage(ReportForRegisteredBusinessPage, true)
          .withPage(HaveGIINPage, false)
          .withPage(UkAddressPage, address)
          .withPage(IsTheAddressCorrectPage, true)
          .withPage(IsThisYourBusinessNamePage, true)
        when(
          mockFinancialInstitutionsService.updateFinancialInstitution(any[String], any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext])
        )
          .thenReturn(Future.successful())
        when(mockFinancialInstitutionUpdateService.clearUserAnswers(any[UserAnswers]))
          .thenReturn(Future.failed(new Exception("failed to clear user answers data")))

        val application = createAppWithAnswers(Option(updatedAnswers))
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
      mockFinancialInstitutionUpdateService.populateAndSaveRegisteredFiDetails(mockitoEq(userAnswers), mockitoEq(fiDetail))
    ).thenReturn(Future.successful((userAnswers, false)))

}
