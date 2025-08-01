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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import models.IndexViewModel
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails}
import models.subscription.response.UserSubscription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import services.{FinancialInstitutionsService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexControllerSpec extends SpecBase {

  val mockSubscriptionService: SubscriptionService                   = mock[SubscriptionService]
  val mockSessionRepository: SessionRepository                       = mock[SessionRepository]
  val mockChangeUserAnswersRepository: ChangeUserAnswersRepository   = mock[ChangeUserAnswersRepository]
  val mockAppConfig: FrontendAppConfig                               = mock[FrontendAppConfig]
  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]

  when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
  when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
  when(mockChangeUserAnswersRepository.get(any())) thenReturn Future.successful(None)
  when(mockChangeUserAnswersRepository.set(any(), any(), any())) thenReturn Future.successful(true)
  when(mockAppConfig.changeIndividualDetailsUrl) thenReturn "/change-contact/individual/details"
  when(mockAppConfig.changeOrganisationDetailsUrl) thenReturn "/change-contact/organisation/details"
  when(mockAppConfig.feedbackUrl(any[RequestHeader]())) thenReturn "test"
  when(mockAppConfig.changeAnswersCacheTtl).thenReturn(3600)

  when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(Future.successful(testFiDetails))

  "Index Controller" - {

    "must redirect to YourFinancialInstitutions page when goToYourFIs is true" in {
      val individualSubscription =
        UserSubscription("FATCAID", None, gbUser = true, ContactInformation(IndividualDetails("firstName", "lastName"), "test@test.com", None), None)
      when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(individualSubscription))

      val application = getApplication

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad(goToYourFIs = true).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.YourFinancialInstitutionsController.onPageLoad().url)

      }
    }

    "must return OK and the correct view for a GET with individualSubscription details" in {
      val individualSubscription =
        UserSubscription("FATCAID", None, gbUser = true, ContactInformation(IndividualDetails("firstName", "lastName"), "test@test.com", None), None)
      val indViewModel = IndexViewModel(
        isBusiness = false,
        "subscriptionId",
        "/change-contact/individual/details",
        None,
        hasFis = true
      )

      when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(individualSubscription))

      val application = getApplication

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(indViewModel)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with organisationSubscription details" in {
      val organisationSubscription =
        UserSubscription("FATCAID", None, gbUser = true, ContactInformation(OrganisationDetails("Test Business inc"), "test@test.com", None), None)
      val orgViewModel = IndexViewModel(
        isBusiness = true,
        "subscriptionId",
        "/change-contact/organisation/details",
        Some("Test Business inc"),
        hasFis = true
      )

      when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(organisationSubscription))

      val application = getApplication

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(orgViewModel)(request, messages(application)).toString
      }
    }
  }

  private def getApplication =
    applicationBuilder(userAnswers = None)
      .overrides(
        bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[FrontendAppConfig].toInstance(mockAppConfig)
      )
      .build()

}
