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
  conf: FrontendAppConfig,
  financialInstitutionsService: FinancialInstitutionsService,
  view: IndexView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>
      val fatcaId = request.fatcaId
      subscriptionService.getSubscription(fatcaId).flatMap {
        sub =>
          val changeContactDetailsUrl       = if (sub.isBusiness) conf.changeOrganisationDetailsUrl else conf.changeIndividualDetailsUrl
          val addNewFIUrl                   = controllers.addFinancialInstitution.routes.AddFIController.onPageLoad.url
          val hasFisFuture: Future[Boolean] = financialInstitutionsService.getListOfFinancialInstitutions(fatcaId).map(_.isEmpty)

          hasFisFuture.flatMap {
            hasFis =>
              val indexPageDetails =
                IndexViewModel(sub.isBusiness, fatcaId, addNewFIUrl, changeContactDetailsUrl, sub.businessName.getOrElse(""), request.userType, hasFis)

              sessionRepository.get(fatcaId) flatMap {
                case Some(_) =>
                  Future.successful(Ok(view(indexPageDetails)))
                case None =>
                  sessionRepository.set(UserAnswers(fatcaId)) map {
                    case true => Ok(view(indexPageDetails))
                    case false =>
                      logger.error(s"Failed to initialize user answers for userId: [$fatcaId]")
                      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                  }
              }
          }
      }
  }

}
