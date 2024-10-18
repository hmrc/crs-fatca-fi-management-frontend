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

import controllers.addFinancialInstitution.routes
import models._
import pages._
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.libs.json.Reads
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case NameOfFinancialInstitutionPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          routes.HaveGIINController.onPageLoad(NormalMode),
          routes.HaveUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
        )
    case WhatIsUniqueTaxpayerReferencePage =>
      _ => routes.HaveGIINController.onPageLoad(NormalMode)
    case WhatIsGIINPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(NormalMode),
          routes.WhereIsFIBasedController.onPageLoad(NormalMode)
        )
    case WhereIsFIBasedPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          WhereIsFIBasedPage,
          routes.PostcodeController.onPageLoad(NormalMode),
          routes.NonUkAddressController.onPageLoad(NormalMode)
        )
    case HaveUniqueTaxpayerReferencePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveUniqueTaxpayerReferencePage,
          routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(NormalMode),
          routes.HaveGIINController.onPageLoad(NormalMode)
        )
    case FirstContactNamePage =>
      _ => routes.FirstContactEmailController.onPageLoad(NormalMode)
    case FirstContactEmailPage => _ => routes.FirstContactHavePhoneController.onPageLoad(NormalMode)
    case FirstContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          FirstContactHavePhonePage,
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
          routes.CheckYourAnswersController.onPageLoad()
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
          routes.CheckYourAnswersController.onPageLoad()
        )
    case SecondContactPhoneNumberPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case PostcodePage                 => addressLookupNavigation(NormalMode)
    case SelectAddressPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad(),
          routes.FirstContactNameController.onPageLoad(NormalMode)
        )
    case IsThisAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisAddressPage,
          isFiUser(
            userAnswers,
            controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad(),
            routes.FirstContactNameController.onPageLoad(NormalMode)
          ),
          routes.UkAddressController.onPageLoad(NormalMode)
        )
    case HaveGIINPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveGIINPage,
          routes.WhatIsGIINController.onPageLoad(NormalMode),
          isFiUser(
            userAnswers,
            controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(NormalMode),
            routes.WhereIsFIBasedController.onPageLoad(NormalMode)
          )
        )
    case ReportForRegisteredBusinessPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          ReportForRegisteredBusinessPage,
          controllers.addFinancialInstitution.registeredBusiness.routes.IsThisYourBusinessNameController.onPageLoad(NormalMode),
          routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
        )
    case IsThisYourBusinessNamePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisYourBusinessNamePage,
          routes.HaveGIINController.onPageLoad(NormalMode),
          routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
        )
    case IsTheAddressCorrectPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsTheAddressCorrectPage,
          controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad(),
          routes.WhereIsFIBasedController.onPageLoad(NormalMode)
        )
    case UkAddressPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad(),
          routes.FirstContactNameController.onPageLoad(NormalMode)
        )
    case NonUkAddressPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad(),
          routes.FirstContactNameController.onPageLoad(NormalMode)
        )
    case RemoveAreYouSurePage => _ => controllers.routes.YourFinancialInstitutionsController.onPageLoad()
    case _ =>
      _ => controllers.routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {

    case HaveUniqueTaxpayerReferencePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveUniqueTaxpayerReferencePage,
          routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(CheckMode),
          redirectToCheckYouAnswers(userAnswers)
        )
    case FirstContactNamePage =>
      userAnswers => resolveNextRoute(userAnswers, routes.FirstContactEmailController.onPageLoad(CheckMode))
    case FirstContactEmailPage =>
      userAnswers => resolveNextRoute(userAnswers, routes.FirstContactHavePhoneController.onPageLoad(CheckMode))
    case FirstContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          FirstContactHavePhonePage,
          routes.FirstContactPhoneNumberController.onPageLoad(CheckMode),
          resolveNextRoute(userAnswers, routes.SecondContactExistsController.onPageLoad(CheckMode))
        )
    case FirstContactPhoneNumberPage =>
      userAnswers => resolveNextRoute(userAnswers, routes.SecondContactExistsController.onPageLoad(CheckMode))
    case SecondContactExistsPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactExistsPage,
          checkNextPageForValueThenRoute(CheckMode, userAnswers, SecondContactNamePage, routes.SecondContactNameController.onPageLoad(CheckMode)),
          resolveAnswersVerificationRoute(userAnswers)
        )
    case SecondContactNamePage =>
      userAnswers => checkNextPageForValueThenRoute(CheckMode, userAnswers, SecondContactEmailPage, routes.SecondContactEmailController.onPageLoad(CheckMode))
    case SecondContactEmailPage =>
      userAnswers =>
        checkNextPageForValueThenRoute(CheckMode, userAnswers, SecondContactCanWePhonePage, routes.SecondContactCanWePhoneController.onPageLoad(CheckMode))
    case SecondContactCanWePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SecondContactCanWePhonePage,
          routes.SecondContactPhoneNumberController.onPageLoad(CheckMode),
          redirectToCheckYouAnswers(userAnswers)
        )
    case SecondContactPhoneNumberPage => redirectToCheckYouAnswers
    case IsTheAddressCorrectPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsTheAddressCorrectPage,
          redirectToCheckYouAnswers(userAnswers),
          routes.WhereIsFIBasedController.onPageLoad(CheckMode)
        )
    case WhereIsFIBasedPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          WhereIsFIBasedPage,
          routes.PostcodeController.onPageLoad(CheckMode),
          routes.NonUkAddressController.onPageLoad(CheckMode)
        )
    case IsThisAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisAddressPage,
          redirectToCheckYouAnswers(userAnswers),
          routes.UkAddressController.onPageLoad(CheckMode)
        )
    case PostcodePage      => addressLookupNavigation(CheckMode)
    case NonUkAddressPage  => redirectToCheckYouAnswers
    case UkAddressPage     => redirectToCheckYouAnswers
    case SelectAddressPage => redirectToCheckYouAnswers
    case HaveGIINPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveGIINPage,
          routes.WhatIsGIINController.onPageLoad(CheckMode),
          redirectToCheckYouAnswers(userAnswers)
        )
    case IsThisYourBusinessNamePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisYourBusinessNamePage,
          redirectToCheckYouAnswers(userAnswers),
          routes.NameOfFinancialInstitutionController.onPageLoad(CheckMode)
        )
    case _ => redirectToCheckYouAnswers
  }

  private def redirectToCheckYouAnswers(ua: UserAnswers): Call = resolveAnswersVerificationRoute(ua)

  private def isFiUser(ua: UserAnswers, yesCall: => Call, noCall: => Call): Call =
    ua.get(ReportForRegisteredBusinessPage) match {
      case Some(value) if value => yesCall
      case _                    => noCall
    }

  private def addressLookupNavigation(mode: Mode)(ua: UserAnswers): Call =
    ua.get(AddressLookupPage) match {
      case Some(value) if value.length == 1 => routes.IsThisAddressController.onPageLoad(mode)
      case _                                => routes.SelectAddressController.onPageLoad(mode)
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

  def checkNextPageForValueThenRoute[A](mode: Mode, userAnswers: UserAnswers, page: QuestionPage[A], call: Call)(implicit rds: Reads[A]): Call =
    if (mode.equals(CheckMode) && userAnswers.get(page).isDefined) resolveAnswersVerificationRoute(userAnswers) else call

  private def resolveAnswersVerificationRoute(userAnswers: UserAnswers): Call = {
    val route = userAnswers.get(ReportForRegisteredBusinessPage) match {
      case Some(value) if value => controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
      case _                    => routes.CheckYourAnswersController.onPageLoad()
    }
    resolveNextRoute(userAnswers, route)
  }

  private def resolveNextRoute(userAnswers: UserAnswers, checkAnswersOnwardRoute: Call): Call =
    (userAnswers.get(ChangeFiDetailsInProgressId), userAnswers.get(ReportForRegisteredBusinessPage)) match {
      case (Some(id), Some(true)) =>
        controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(id)
      case (Some(id), _) =>
        controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(id)
      case _ => checkAnswersOnwardRoute
    }

}
