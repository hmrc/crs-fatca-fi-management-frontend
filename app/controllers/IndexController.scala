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

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import models.{IndexViewModel, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{FinancialInstitutionsService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  subscriptionService: SubscriptionService,
  financialInstitutionsService: FinancialInstitutionsService,
  view: IndexView
)(implicit ec: ExecutionContext, conf: FrontendAppConfig)
    extends FrontendBaseController
    with Logging
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>
      sessionRepository.set(UserAnswers.apply(request.userId))
      val fatcaId = request.fatcaId
      subscriptionService.getSubscription(fatcaId).flatMap {
        sub =>
          if (sub.isBusiness && sub.businessName.isEmpty) {
            logger.error(s"BusinessName is missing")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          } else {
            financialInstitutionsService
              .getListOfFinancialInstitutions(fatcaId)
              .map(_.nonEmpty)
              .flatMap {
                hasFis =>
                  val changeContactDetailsUrl = if (sub.isBusiness) conf.changeOrganisationDetailsUrl else conf.changeIndividualDetailsUrl
                  Future.successful(Ok(view(IndexViewModel(sub.isBusiness, fatcaId, changeContactDetailsUrl, sub.businessName, hasFis))))

              }
          }

      }

  }

}
