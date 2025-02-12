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
import models.FinancialInstitutions.FIDetail
import models.subscription.response.UserSubscription
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FinancialInstitutionsService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.UserAccessView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAccessController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  formProvider: UserAccessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  subscriptionService: SubscriptionService,
  financialInstitutionsService: FinancialInstitutionsService,
  view: UserAccessView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  def onPageLoad(fiid: String): Action[AnyContent] = identify.async {
    implicit request =>
      val fatcaId = request.fatcaId

      subscriptionService.getSubscription(fatcaId).flatMap {
        sub =>
          financialInstitutionsService.getListOfFinancialInstitutions(fatcaId).flatMap {
            institutions =>
              financialInstitutionsService.getInstitutionById(institutions, fiid) match {
                case Some(institutionToRemove) =>
                  val key: String = getAccessType(sub, institutionToRemove)
                  Future.successful(
                    Ok(
                      view(
                        formProvider(key),
                        sub.isBusiness,
                        institutionToRemove.IsFIUser,
                        institutionToRemove.FIID,
                        institutionToRemove.FIName,
                        sub.businessName.getOrElse("your business")
                      )
                    )
                  )
                case None =>
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
          }
      }
  }

  def onSubmit(fiid: String): Action[AnyContent] = identify.async {
    implicit request =>
      val fatcaId = request.fatcaId
      subscriptionService.getSubscription(fatcaId).flatMap {
        sub =>
          financialInstitutionsService.getListOfFinancialInstitutions(fatcaId).flatMap {
            institutions =>
              financialInstitutionsService.getInstitutionById(institutions, fiid) match {
                case Some(institutionToRemove) =>
                  val key: String = getAccessType(sub, institutionToRemove)
                  formProvider(key)
                    .bindFromRequest()
                    .fold(
                      formWithErrors =>
                        Future.successful(
                          BadRequest(
                            view(
                              formWithErrors,
                              sub.isBusiness,
                              institutionToRemove.IsFIUser,
                              institutionToRemove.FIID,
                              institutionToRemove.FIName,
                              sub.businessName.getOrElse("your business")
                            )
                          )
                        ),
                      _ => Future.successful(Redirect(routes.IndexController.onPageLoad())) // todo change to /remove/other-access page when made
                    )
                case None =>
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
          }
      }
  }

  private def getAccessType(sub: UserSubscription, institutionToRemove: FIDetail): String = {
    val key = (sub.isBusiness, institutionToRemove.IsFIUser) match {
      case (true, true)  => "registeredUser"
      case (true, false) => "organisation"
      case (false, _)    => "individual"
    }
    key
  }

}
