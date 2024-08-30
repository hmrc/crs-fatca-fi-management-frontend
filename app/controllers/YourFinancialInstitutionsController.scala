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

import controllers.actions._
import forms.addFinancialInstitution.YourFinancialInstitutionsFormProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.yourFinancialInstitutions.YourFinancialInstitutionsViewModel.getYourFinancialInstitutionsRows
import views.html.YourFinancialInstitutionsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourFinancialInstitutionsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: YourFinancialInstitutionsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  val financialInstitutionsService: FinancialInstitutionsService,
  view: YourFinancialInstitutionsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId) map {
        institutions => Ok(view(form, SummaryListViewModel(getYourFinancialInstitutionsRows(institutions))))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId) map {
              institutions => Ok(view(formWithErrors, SummaryListViewModel(getYourFinancialInstitutionsRows(institutions))))
            },
          {
            case true  => Future.successful(Redirect(controllers.addFinancialInstitution.routes.AddFIController.onPageLoad))
            case false => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad()))
          }
        )
  }

}
