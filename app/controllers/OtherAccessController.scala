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

import controllers.actions._
import forms.OtherAccessFormProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.OtherAccessView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAccessController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: OtherAccessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  financialInstitutionsService: FinancialInstitutionsService,
  view: OtherAccessView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(fiid: String): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId).flatMap {
        institutions =>
          financialInstitutionsService.getInstitutionById(institutions, fiid) match {
            case Some(institutionToRemove) =>
              Future.successful(
                Ok(view(form, institutionToRemove.IsFIUser, institutionToRemove.FIID, institutionToRemove.FIName))
              )
            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }

  }

  def onSubmit(fiid: String): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId).flatMap {
        institutions =>
          financialInstitutionsService.getInstitutionById(institutions, fiid) match {
            case Some(institutionToRemove) =>
              form
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(
                        view(formWithErrors, institutionToRemove.IsFIUser, institutionToRemove.FIID, institutionToRemove.FIName)
                      )
                    ),
                  _ => Future.successful(Redirect(routes.IndexController.onPageLoad())) // todo change to /remove/remove-fi page when made
                )
            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }
  }

}
