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

package controllers.addFinancialInstitution.registeredBusiness

import controllers.actions._
import controllers.routes
import forms.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNameFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisYourBusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subscriptionService: SubscriptionService,
  formProvider: IsThisYourBusinessNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisYourBusinessNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subscriptionService.getSubscription(request.fatcaId).flatMap {
        sub =>
          val result = sub.businessName match {
            case None =>
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            case Some(fiName) =>
              val preparedForm = request.userAnswers.get(IsThisYourBusinessNamePage) match {
                case None => form
                case Some(_) =>
                  val nameMatches = request.userAnswers.get(NameOfFinancialInstitutionPage).contains(fiName)
                  form.fill(nameMatches)
              }

              Ok(view(preparedForm, mode, fiName))
          }

          Future.successful(result)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subscriptionService.getSubscription(request.fatcaId).flatMap {
        sub =>
          sub.businessName match {
            case None =>
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            case Some(fiName) =>
              form
                .bindFromRequest()
                .fold(
                  formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiName))),
                  value =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessNamePage, value))
                      updatedFIName  <- setFIName(value, fiName, updatedAnswers)
                      _              <- sessionRepository.set(updatedFIName)
                    } yield Redirect(navigator.nextPage(IsThisYourBusinessNamePage, mode, updatedAnswers))
                )
          }
      }
  }

  private def setFIName(isThisYourBusinessName: Boolean, fiName: String, userAnswers: UserAnswers): Future[UserAnswers] =
    if (isThisYourBusinessName) {
      Future.fromTry(userAnswers.set(NameOfFinancialInstitutionPage, fiName))
    } else { Future.successful(userAnswers) }

}
