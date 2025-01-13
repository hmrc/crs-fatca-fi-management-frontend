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
import forms.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNameFormProvider
import models.NormalMode
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails}
import models.subscription.response.UserSubscription
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNameView

import scala.concurrent.{ExecutionContext, Future}

class IsThisYourBusinessNameControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new IsThisYourBusinessNameFormProvider()
  val form: Form[Boolean] = formProvider()

  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

  lazy val isThisYourBusinessNameRoute: String =
    controllers.addFinancialInstitution.registeredBusiness.routes.IsThisYourBusinessNameController.onPageLoad(NormalMode).url

  val organisationSubscription: UserSubscription =
    UserSubscription("FATCAID", None, gbUser = true, ContactInformation(OrganisationDetails("testName"), "test@test.com", None), None)

  val individualSubscription: UserSubscription =
    UserSubscription("FATCAID", None, gbUser = true, ContactInformation(IndividualDetails("firstname", "lastname"), "test@test.com", None), None)

  "IsThisYourBusinessName Controller" - {

    "if the user has an organisation subscription" - {

      "must return OK and the correct view for a GET" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(organisationSubscription))

        val userAnswers = emptyUserAnswers
          .withPage(NameOfFinancialInstitutionPage, "testName")

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, isThisYourBusinessNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[IsThisYourBusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "testName")(request, messages(application)).toString
        }
      }

      "must populate the view with TRUE on a GET when fetched name does match name in User Answers" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(organisationSubscription))

        val userAnswers = emptyUserAnswers
          .withPage(IsThisYourBusinessNamePage, true)
          .withPage(NameOfFinancialInstitutionPage, "testName")

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, isThisYourBusinessNameRoute)

          val view = application.injector.instanceOf[IsThisYourBusinessNameView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), NormalMode, "testName")(request, messages(application)).toString
        }
      }

      "must populate the view with FALSE on a GET when fetched name doesn't match name in User Answers" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(organisationSubscription))

        val userAnswers = emptyUserAnswers
          .withPage(IsThisYourBusinessNamePage, true)
          .withPage(NameOfFinancialInstitutionPage, "testNameFail")

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, isThisYourBusinessNameRoute)

          val view = application.injector.instanceOf[IsThisYourBusinessNameView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(false), NormalMode, "testName")(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(organisationSubscription))

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubscriptionService].toInstance(mockSubscriptionService),
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, isThisYourBusinessNameRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to information-sent page for a GET when the user answers is empty" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(organisationSubscription))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, isThisYourBusinessNameRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.InformationSentController.onPageLoad.url
        }
      }

    }

    "if the user has an individual subscription" - {

      "must redirect to journey recovery for a GET" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(individualSubscription))

        val userAnswers = emptyUserAnswers
          .withPage(NameOfFinancialInstitutionPage, "testName")

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, isThisYourBusinessNameRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to journey recovery for a POST" in {
        when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(individualSubscription))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, isThisYourBusinessNameRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }

  }

}
