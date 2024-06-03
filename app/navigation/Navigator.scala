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
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case NameOfFinancialInstitutionPage =>
      _ => routes.HaveUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
    case WhatIsUniqueTaxpayerReferencePage =>
      _ => routes.SendReportsController.onPageLoad(NormalMode)
    case SendReportsPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          SendReportsPage,
          routes.WhatIsGIINController.onPageLoad(NormalMode),
          routes.HaveGIINController.onPageLoad(NormalMode)
        )
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
          routes.SendReportsController.onPageLoad(NormalMode)
        )
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
    case PostcodePage                 => addressLookupNavigation(NormalMode)
    case SelectAddressPage            => _ => routes.ContactNameController.onPageLoad(NormalMode)
    case IsThisAddressPage =>
      userAnswers =>
        yesNoPage(
          userAnswers,
          IsThisAddressPage,
          routes.ContactNameController.onPageLoad(NormalMode),
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
          routes.SendReportsController.onPageLoad(NormalMode),
          routes.NameOfFinancialInstitutionController.onPageLoad(NormalMode)
        )
    case UkAddressPage    => _ => routes.ContactNameController.onPageLoad(NormalMode)
    case NonUkAddressPage => _ => routes.ContactNameController.onPageLoad(NormalMode)
    case _ =>
      _ => controllers.routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = _ => _ => routes.CheckYourAnswersController.onPageLoad

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

}
