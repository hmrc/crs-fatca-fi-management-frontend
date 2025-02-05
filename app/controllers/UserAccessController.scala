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
import forms.UserAccessFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.UserAccessPage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.UserAccessView

import scala.concurrent.{ExecutionContext, Future}

class UserAccessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UserAccessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  subscriptionService: SubscriptionService,
  view: UserAccessView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val fatcaId = request.fatcaId
      subscriptionService.getSubscription(fatcaId).flatMap {
        sub =>
          if (sub.isBusiness && sub.businessName.isEmpty) {
            logger.error(s"BusinessName is missing")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          } else {
            val ua = request.userAnswers
            val preparedForm = ua.get(UserAccessPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            val isRegistered: Boolean = request.userAnswers.get(ReportForRegisteredBusinessPage).contains(true)

            sessionRepository.get(fatcaId) flatMap {
              case Some(_) =>
                Ok(view(sub.isBusiness, isRegistered, getFirstContactName(ua), getFinancialInstitutionName(ua), preparedForm, mode))
              case None =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UserAccessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(UserAccessPage, mode, updatedAnswers))
        )
  }

}
