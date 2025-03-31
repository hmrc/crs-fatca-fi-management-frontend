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
import models.FinancialInstitutions.TINType
import models.FinancialInstitutions.TINType._
import models._
import pages._
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.libs.json.Reads
import play.api.mvc.Call
import utils.CheckYourAnswersValidator

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case NameOfFinancialInstitutionPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          routes.HaveGIINController.onPageLoad(NormalMode),
          routes.WhichIdentificationNumbersController.onPageLoad(NormalMode)
        )
    case WhichIdentificationNumbersPage => userAnswers => whichIdPage(userAnswers)
    case WhatIsUniqueTaxpayerReferencePage =>
      userAnswers =>
        userAnswers.get(WhichIdentificationNumbersPage) match {
          case Some(identificationNumbers) if identificationNumbers.contains(CRN) =>
            routes.WhatIsCompanyRegistrationNumberController.onPageLoad(NormalMode)
          case _ =>
            routes.HaveGIINController.onPageLoad(NormalMode)
        }
    case CompanyRegistrationNumberPage => _ => routes.HaveGIINController.onPageLoad(NormalMode)
    case TrustURNPage                  => _ => routes.HaveGIINController.onPageLoad(NormalMode)
    case WhatIsGIINPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(NormalMode),
          controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(NormalMode)
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
          resolveNextRouteForChangeFIJourney(userAnswers, routes.CheckYourAnswersController.onPageLoad())
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
          resolveNextRouteForChangeFIJourney(userAnswers, routes.CheckYourAnswersController.onPageLoad())
        )
    case SecondContactPhoneNumberPage => userAnswers => resolveNextRouteForChangeFIJourney(userAnswers, routes.CheckYourAnswersController.onPageLoad())
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
            controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(NormalMode)
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
          getRegisteredFIRoute(userAnswers),
          controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(NormalMode)
        )
    case UkAddressPage =>
      userAnswers =>
        isFiUser(
          userAnswers,
          getRegisteredFIRoute(userAnswers),
          routes.FirstContactNameController.onPageLoad(NormalMode)
        )
    case RemoveAreYouSurePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          RemoveAreYouSurePage,
          controllers.routes.FIRemovedController.onPageLoad(),
          controllers.routes.YourFinancialInstitutionsController.onPageLoad()
        )
    case _ =>
      _ => controllers.routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case ReportForRegisteredBusinessPage =>
      userAnswers =>
        def resolveRoute(userAnswers: UserAnswers, registeredBusinessRoute: Boolean) = {
          val validator        = CheckYourAnswersValidator(userAnswers)
          val validationResult = if (registeredBusinessRoute) validator.validateRegisteredBusiness else validator.validate

          validationResult match {
            case Nil => redirectToCheckYourAnswers(userAnswers)
            case _ if registeredBusinessRoute =>
              controllers.addFinancialInstitution.registeredBusiness.routes.IsThisYourBusinessNameController.onPageLoad(NormalMode)
            case _ =>
              controllers.addFinancialInstitution.routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
          }
        }
        yesNoPage(
          userAnswers,
          ReportForRegisteredBusinessPage,
          resolveRoute(userAnswers, registeredBusinessRoute = true),
          resolveRoute(userAnswers, registeredBusinessRoute = false)
        )
    case FirstContactHavePhonePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          FirstContactHavePhonePage,
          routes.FirstContactPhoneNumberController.onPageLoad(CheckMode),
          redirectToCheckYourAnswers(userAnswers)
        )
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
          redirectToCheckYourAnswers(userAnswers)
        )
    case IsTheAddressCorrectPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsTheAddressCorrectPage,
          redirectToCheckYourAnswers(userAnswers),
          controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(CheckMode)
        )
    case IsThisAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisAddressPage,
          redirectToCheckYourAnswers(userAnswers),
          routes.UkAddressController.onPageLoad(CheckMode)
        )
    case PostcodePage => addressLookupNavigation(CheckMode)
    case HaveGIINPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          HaveGIINPage,
          routes.WhatIsGIINController.onPageLoad(CheckMode),
          redirectToCheckYourAnswers(userAnswers)
        )
    case IsThisYourBusinessNamePage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisYourBusinessNamePage,
          redirectToCheckYourAnswers(userAnswers),
          routes.NameOfFinancialInstitutionController.onPageLoad(CheckMode)
        )
    case WhichIdentificationNumbersPage => changeWhichIdPage
    case WhatIsUniqueTaxpayerReferencePage =>
      userAnswers =>
        userAnswers
          .get(WhichIdentificationNumbersPage)
          .fold(controllers.routes.JourneyRecoveryController.onPageLoad()) {
            identificationNumbers =>
              if (
                identificationNumbers.contains(UTR) &&
                identificationNumbers.contains(CRN) &&
                userAnswers.get(CompanyRegistrationNumberPage).isEmpty
              ) routes.WhatIsCompanyRegistrationNumberController.onPageLoad(CheckMode)
              else redirectToCheckYourAnswers(userAnswers)
          }

    case _ => redirectToCheckYourAnswers
  }

  def redirectToCheckYourAnswers(ua: UserAnswers): Call = resolveAnswersVerificationRoute(ua)

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

  private def whichIdPage(ua: UserAnswers): Call =
    ua.get(WhichIdentificationNumbersPage).fold(controllers.routes.JourneyRecoveryController.onPageLoad()) {
      case set if set.contains(UTR) => routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      case set if set.contains(CRN) => routes.WhatIsCompanyRegistrationNumberController.onPageLoad(NormalMode)
      case set if set.contains(TRN) => routes.TrustURNController.onPageLoad(NormalMode)
      case _                        => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  private def changeWhichIdPage(ua: UserAnswers): Call = {
    def isMissing[A](page: QuestionPage[A])(implicit reads: Reads[A]): Boolean =
      ua.get(page).isEmpty

    def handleCombinedTINs(set: Set[TINType]): Option[Call] =
      (set.contains(UTR), set.contains(CRN)) match {
        case (true, true) if isMissing(WhatIsUniqueTaxpayerReferencePage) =>
          Some(routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(CheckMode))
        case (true, true) if isMissing(CompanyRegistrationNumberPage) =>
          Some(routes.WhatIsCompanyRegistrationNumberController.onPageLoad(CheckMode))
        case _ => None
      }
    def handleSingleCases(selectedTINs: Set[TINType]): Option[Call] =
      selectedTINs.collectFirst {
        case UTR if isMissing(WhatIsUniqueTaxpayerReferencePage) =>
          routes.WhatIsUniqueTaxpayerReferenceController.onPageLoad(CheckMode)
        case CRN if isMissing(CompanyRegistrationNumberPage) =>
          routes.WhatIsCompanyRegistrationNumberController.onPageLoad(CheckMode)
        case TRN if isMissing(TrustURNPage) =>
          routes.TrustURNController.onPageLoad(CheckMode)
      }

    ua.get(WhichIdentificationNumbersPage).fold(controllers.routes.JourneyRecoveryController.onPageLoad()) {
      selectedTINs =>
        handleCombinedTINs(selectedTINs).orElse(handleSingleCases(selectedTINs)).getOrElse(redirectToCheckYourAnswers(ua))
    }
  }

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

  private def resolveNextRouteForChangeFIJourney(userAnswers: UserAnswers, checkAnswersOnwardRoute: Call): Call =
    userAnswers.get(ChangeFiDetailsInProgressId) match {
      case Some(id) => controllers.changeFinancialInstitution.routes.ChangeFinancialInstitutionController.onPageLoad(id)
      case _        => checkAnswersOnwardRoute
    }

  private def getRegisteredFIRoute(userAnswers: UserAnswers): Call =
    userAnswers.get(ChangeFiDetailsInProgressId) match {
      case Some(id) => controllers.changeFinancialInstitution.routes.ChangeRegisteredFinancialInstitutionController.onPageLoad(id)
      case None     => controllers.addFinancialInstitution.registeredBusiness.routes.RegisteredBusinessCheckYourAnswersController.onPageLoad()
    }

}
