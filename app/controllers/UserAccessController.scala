package controllers

import controllers.actions._
import forms.UserAccessFormProvider

import javax.inject.Inject
import models.{Mode, ReporterType}
import navigation.Navigator
import pages.addFinancialInstitution.IsRegisteredBusiness.ReportForRegisteredBusinessPage
import pages.{ReporterTypePage, UserAccessPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UserAccessView

import scala.concurrent.{ExecutionContext, Future}

class UserAccessController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: UserAccessFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: UserAccessView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(UserAccessPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val individual: Boolean = request.userAnswers.get(ReporterTypePage).exists(ReporterType.nonOrgReporterTypes.contains)
      val organisation: Boolean = request.userAnswers.get(ReporterTypePage).exists(ReporterType.orgReporterTypes.contains)
      val isRegistered: Boolean = request.userAnswers.get(ReportForRegisteredBusinessPage)

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UserAccessPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(UserAccessPage, mode, updatedAnswers))
      )
  }
}
