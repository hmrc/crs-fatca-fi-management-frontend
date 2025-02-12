/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.SubscriptionConnector
import controllers.actions.IdentifierAction
import forms.UserAccessFormProvider
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails}
import models.subscription.response.UserSubscription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.PrivateMethodTester
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FinancialInstitutionsService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.UserAccessView

import scala.concurrent.{ExecutionContext, Future}

class UserAccessControllerSpec extends SpecBase with MockitoSugar with PrivateMethodTester {

  val formProvider        = new UserAccessFormProvider()
  val form: Form[Boolean] = formProvider("registeredUser")

  val fiIsUser   = true
  val isBusiness = true

  val organisationSubscription: UserSubscription =
    UserSubscription("FATCAID", None, gbUser = true, ContactInformation(OrganisationDetails("User Business"), "test@test.com", None), None)

  val individualSubscription: UserSubscription =
    UserSubscription("FATCAID", None, gbUser = true, ContactInformation(IndividualDetails("firstname", "lastname"), "test@test.com", None), None)

  lazy val userAccessRoute: String       = routes.UserAccessController.onPageLoad(testFiid).url
  lazy val userAccessSubmitRoute: String = routes.UserAccessController.onSubmit(testFiid).url

  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]
  val mockSubscriptionService: SubscriptionService                   = mock[SubscriptionService]
  val mockSubConnector: SubscriptionConnector                        = mock[SubscriptionConnector]

  when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(Future.successful(testFiDetails))

  when(mockFinancialInstitutionsService.getInstitutionById(Seq(any()), any())).thenReturn(Some(testFiDetail))

  when(mockSubscriptionService.getSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
    .thenReturn(Future.successful(organisationSubscription))

  "UserAccess Controller" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder()
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, userAccessRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UserAccessView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, isBusiness, fiIsUser, testFiDetail.FIID, testFiDetail.FIName, testBusinessName)(request,
                                                                                                                                     messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, userAccessSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, userAccessRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UserAccessView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isBusiness, fiIsUser, testFiid, testFiDetail.FIName, testBusinessName)(request,
                                                                                                                                 messages(application)
        ).toString
      }
    }

    "must return a Recover Journey when no FI found" in {

      when(mockFinancialInstitutionsService.getInstitutionById(Seq(any()), any())).thenReturn(None)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, userAccessRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }

  val userAccessTestController = new UserAccessController(
    messagesApi = stubMessagesApi(),
    identify = mock[IdentifierAction],
    formProvider = formProvider,
    controllerComponents = stubMessagesControllerComponents(),
    subscriptionService = mock[SubscriptionService],
    financialInstitutionsService = mock[FinancialInstitutionsService],
    view = mock[UserAccessView]
  )(ExecutionContext.global)

  val getAccessType: PrivateMethod[String] = PrivateMethod[String](Symbol("getAccessType"))

  "getAccessType" - {
    "return 'individual' when the user is not registered" in {
      val result: String = userAccessTestController.invokePrivate(getAccessType(individualSubscription, testFiDetail))
      result mustEqual "individual"
    }

    "return 'organisation' when the user is registered but not an organisation" in {
      val result: String = userAccessTestController.invokePrivate(getAccessType(organisationSubscription, testFiDetail.copy(IsFIUser = false)))
      result mustEqual "organisation"
    }

    "return 'registeredUser' when the user is both registered and an organisation" in {
      val result: String = userAccessTestController.invokePrivate(getAccessType(organisationSubscription, testFiDetail))
      result mustEqual "registeredUser"
    }

  }

}
