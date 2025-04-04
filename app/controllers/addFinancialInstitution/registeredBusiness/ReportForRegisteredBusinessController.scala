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
import forms.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessFormProvider
import models.Mode
import navigation.Navigator
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportForRegisteredBusinessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  changeUserAnswersRepository: ChangeUserAnswersRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReportForRegisteredBusinessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReportForRegisteredBusinessView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ReportForRegisteredBusinessPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val isChangeFIInProgress = request.userAnswers.get(ChangeFiDetailsInProgressId) match {
        case Some(_) => true
        case None    => false
      }

      Ok(view(preparedForm, mode, isChangeFIInProgress))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val isChangeFIInProgress = request.userAnswers.get(ChangeFiDetailsInProgressId) match {
        case Some(_) => true
        case _       => false
      }
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isChangeFIInProgress))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ReportForRegisteredBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
              _              <- changeUserAnswersRepository.set(request.fatcaId, updatedAnswers.get(ChangeFiDetailsInProgressId), updatedAnswers)
            } yield Redirect(navigator.nextPage(ReportForRegisteredBusinessPage, mode, updatedAnswers))
        )
  }

}
