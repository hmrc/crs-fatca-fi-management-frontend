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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case NameOfFinancialInstitutionPage =>
      _ => routes.HaveUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
    case HaveUniqueTaxpayerReferencePage =>
      userAnswers =>
        yesNoPage(userAnswers,
                  HaveUniqueTaxpayerReferencePage,
                  routes.IndexController.onPageLoad, // todo what is UTR page
                  routes.IndexController.onPageLoad
        ) // todo does FR need to send reports
    case ContactNamePage =>
      _ => routes.FirstContactEmailController.onPageLoad(NormalMode)
    case FirstContactEmailPage => _ => routes.ContactHavePhoneController.onPageLoad(NormalMode)
    case ContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          ContactHavePhonePage,
          routes.FirstContactPhoneNumberController.onPageLoad(NormalMode),
          routes.SecondContactExistsController.onPageLoad(NormalMode)
        )
    case FirstContactPhoneNumberPage => _ => routes.SecondContactExistsController.onPageLoad(NormalMode)
    case SecondContactExistsPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactExistsPage,
          routes.SecondContactNameController.onPageLoad(NormalMode),
          routes.CheckYourAnswersController.onPageLoad
        )
    case SecondContactNamePage =>
      _ => routes.SecondContactEmailController.onPageLoad(NormalMode)
    case SecondContactEmailPage =>
      _ => routes.SecondContactCanWePhoneController.onPageLoad(NormalMode)
    case SecondContactCanWePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactCanWePhonePage,
          routes.SecondContactPhoneNumberController.onPageLoad(NormalMode),
          routes.CheckYourAnswersController.onPageLoad
        )
    case SecondContactPhoneNumberPage => _ => routes.CheckYourAnswersController.onPageLoad
    case _ =>
      _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ =>
      _ => routes.CheckYourAnswersController.onPageLoad
  }

  private def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.JourneyRecoveryController.onPageLoad())

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

}
