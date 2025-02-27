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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.OtherAccessFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.PrivateMethodTester
import org.scalatestplus.mockito.MockitoSugar
import pages.InstitutionDetail
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.OtherAccessView

import scala.concurrent.{ExecutionContext, Future}

class OtherAccessControllerSpec extends SpecBase with MockitoSugar with PrivateMethodTester {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new OtherAccessFormProvider()
  val form: Form[Boolean] = formProvider("fiisuser")
  val fiIsUser            = true

  lazy val otherAccessRoute: String                                  = routes.OtherAccessController.onPageLoad(testFiDetail.FIID).url
  lazy val otherAccessPostRoute: String                              = routes.OtherAccessController.onSubmit().url
  val mockFinancialInstitutionsService: FinancialInstitutionsService = mock[FinancialInstitutionsService]
  val mockSessionRepository: SessionRepository                       = mock[SessionRepository]
  val ua: UserAnswers                                                = emptyUserAnswers.withPage(InstitutionDetail, testFiDetail)

  "OtherAccess Controller" - {

    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
    when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
      .thenReturn(Future.successful(testFiDetails))
    when(mockFinancialInstitutionsService.getInstitutionById(Seq(any()), any())).thenReturn(Some(testFiDetail))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, otherAccessRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OtherAccessView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, fiIsUser, testFiDetail.FIName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, otherAccessPostRoute)
            .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RemoveAreYouSureController.onPageLoad().url
      }
    }
    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, otherAccessPostRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[OtherAccessView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, fiIsUser, testFiDetail.FIName)(request, messages(application)).toString
      }
    }

  }

  val OtherAccessTestController = new OtherAccessController(
    messagesApi = stubMessagesApi(),
    identify = mock[IdentifierAction],
    formProvider = formProvider,
    controllerComponents = stubMessagesControllerComponents(),
    getData = mock[DataRetrievalAction],
    requireData = mock[DataRequiredAction],
    financialInstitutionsService = mock[FinancialInstitutionsService],
    view = mock[OtherAccessView],
    sessionRepository = mock[SessionRepository]
  )(ExecutionContext.global)

  val getFormKey: PrivateMethod[String] = PrivateMethod[String](Symbol("getFormKey"))

  "getFormKey" - {
    "return 'fiisuser' when the FI is the registered user" in {
      val result: String = OtherAccessTestController.invokePrivate(getFormKey(testFiDetail))
      result mustEqual "fiisuser"
    }

    "return 'regular' when the user is not the registered user" in {
      val result: String = OtherAccessTestController.invokePrivate(getFormKey(testFiDetail.copy(IsFIUser = false)))
      result mustEqual "regular"
    }
  }

}
