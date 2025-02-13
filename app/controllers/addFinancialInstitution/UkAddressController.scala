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

package controllers.addFinancialInstitution

import controllers.actions._
import forms.addFinancialInstitution.UkAddressFormProvider
import models.{Country, Mode}
import navigation.Navigator
import pages.addFinancialInstitution.UkAddressPage
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ContactHelper, CountryListFactory}
import views.html.addFinancialInstitution.UkAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkAddressController @Inject() (
  override val messagesApi: MessagesApi,
  countryListFactory: CountryListFactory,
  sessionRepository: SessionRepository,
  changeUserAnswersRepository: ChangeUserAnswersRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UkAddressFormProvider,
  checkForInformationSentAction: CheckForInformationSentAction,
  val controllerComponents: MessagesControllerComponents,
  view: UkAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging
    with ContactHelper
    with I18nSupport {

  lazy val countriesList: Seq[Country] = countryListFactory.countryListWithUKCountries

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkForInformationSentAction) {
    implicit request =>
      val form = formProvider()
      val preparedForm = request.userAnswers.get(UkAddressPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(
        view(
          preparedForm,
          getFinancialInstitutionName(request.userAnswers),
          mode
        )
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  getFinancialInstitutionName(request.userAnswers),
                  mode
                )
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UkAddressPage, value))
              _              <- sessionRepository.set(updatedAnswers)
              _              <- changeUserAnswersRepository.set(request.fatcaId, updatedAnswers.get(ChangeFiDetailsInProgressId), updatedAnswers)
            } yield Redirect(navigator.nextPage(UkAddressPage, mode, updatedAnswers))
        )
  }

}
