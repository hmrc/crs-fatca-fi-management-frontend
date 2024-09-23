package controllers

import controllers.actions._
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.{DetailsUpdatedView, FinancialInstitutionAddedConfirmationView, ThereIsAProblemView}

import scala.concurrent.{ExecutionContext, Future}

class DetailsUpdatedController @Inject() (
                                                                  override val messagesApi: MessagesApi,
                                                                  identify: IdentifierAction,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  sessionRepository: SessionRepository,
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  view: DetailsUpdatedView,
                                                                  errorView: ThereIsAProblemView
                                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with ContactHelper
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val fiName = getFinancialInstitutionName(request.userAnswers)
      sessionRepository.set(request.userAnswers.copy(data = Json.obj())).flatMap {
        case true => Future.successful(Ok(view(fiName)))
        case false =>
          logger.error(s"Failed to clear user answers after adding an FI for userId: [${request.userId}]")
          Future.successful(Ok(errorView()))
      }
  }

}
