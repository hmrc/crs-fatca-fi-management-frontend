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
import forms.addFinancialInstitution.YourFinancialInstitutionsFormProvider
import generators.ModelGenerators
import models.FinancialInstitutions.FIDetail
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FinancialInstitutionUpdateService, FinancialInstitutionsService}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.listwithactions.ListWithActions
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.yourFinancialInstitutions.YourFinancialInstitutionsViewModel
import views.html.YourFinancialInstitutionsView

import scala.concurrent.{ExecutionContext, Future}

class YourFinancialInstitutionsControllerSpec extends SpecBase with MockitoSugar with ModelGenerators with ScalaCheckDrivenPropertyChecks {

  val formProvider = new YourFinancialInstitutionsFormProvider()
  val form         = formProvider()

  "YourFinancialInstitutionsControllerSpec" - {

    "must return ok and correct view for a GET" in {

      val mockFinancialInstitutionsService = mock[FinancialInstitutionsService]
      when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Seq.empty))

      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers))
        .overrides(
          bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.YourFinancialInstitutionsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[YourFinancialInstitutionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, ListWithActions(Seq.empty))(request, messages(application)).toString
      }
    }

    "must list FIs as non-registered for non-CT user" in {
      forAll {
        fiDetail: FIDetail =>
          val mockFinancialInstitutionsService = mock[FinancialInstitutionsService]
          when(mockFinancialInstitutionsService.getListOfFinancialInstitutions(any())(any[HeaderCarrier](), any[ExecutionContext]()))
            .thenReturn(Future.successful(Seq(fiDetail)))

          val application = applicationBuilder(userAnswers = Option(emptyUserAnswers))
            .overrides(
              bind[FinancialInstitutionsService].toInstance(mockFinancialInstitutionsService)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.YourFinancialInstitutionsController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[YourFinancialInstitutionsView]

            val institutions = YourFinancialInstitutionsViewModel.getYourFinancialInstitutionsRows(Seq(fiDetail.copy(IsFIUser = false)))(messages(application))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, institutions)(request, messages(application)).toString
          }
      }
    }

    "must redirect to Add new FI when user selects yes" in {

      val mockFinancialInstitutionUpdateService = mock[FinancialInstitutionUpdateService]
      when(mockFinancialInstitutionUpdateService.clearUserAnswers(any[UserAnswers])).thenReturn(Future.successful(true))
      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers))
        .overrides(
          bind[FinancialInstitutionUpdateService].toInstance(mockFinancialInstitutionUpdateService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.YourFinancialInstitutionsController.onSubmit().url).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.addFinancialInstitution.routes.AddFIController.onPageLoad.url
        verify(mockFinancialInstitutionUpdateService).clearUserAnswers(any[UserAnswers])
      }
    }

    "must redirect to Index Page when user selects no" in {

      val application = applicationBuilder(userAnswers = Option(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.YourFinancialInstitutionsController.onSubmit().url).withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad().url
      }
    }
  }

}
