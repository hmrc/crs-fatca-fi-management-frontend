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
import forms.RemoveAreYouSureFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.{RemoveAreYouSurePage, RemoveInstitutionDetail}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.FinancialInstitutionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.RemoveAreYouSureView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAreYouSureController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveAreYouSureFormProvider,
  val controllerComponents: MessagesControllerComponents,
  val financialInstitutionsService: FinancialInstitutionsService,
  view: RemoveAreYouSureView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(fiid: String): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId).flatMap {
        institutions =>
          financialInstitutionsService.getInstitutionById(institutions, fiid) match {
            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            case Some(institutionToRemove) =>
              for {
                updatedAnswers <- Future.fromTry(UserAnswers(id = request.userId).set(RemoveInstitutionDetail, institutionToRemove))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Ok(view(form, institutionToRemove.FIID, institutionToRemove.FIName))
          }
      }

  }

  def onSubmit(fiid: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      financialInstitutionsService.getListOfFinancialInstitutions(request.fatcaId).flatMap {
        institutions =>
          financialInstitutionsService.getInstitutionById(institutions, fiid) match {
            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            case Some(institutionToRemove) =>
              form
                .bindFromRequest()
                .fold(
                  formWithErrors => Future.successful(BadRequest(view(formWithErrors, institutionToRemove.FIID, institutionToRemove.FIName))),
                  value =>
                    for {
                      _              <- if (value) financialInstitutionsService.removeFinancialInstitution(institutionToRemove) else Future.successful(())
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveAreYouSurePage, value))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(RemoveAreYouSurePage, NormalMode, updatedAnswers))
                )
          }
      }
  }

}
