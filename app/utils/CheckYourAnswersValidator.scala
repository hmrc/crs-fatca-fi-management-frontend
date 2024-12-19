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

package utils

import models.{CheckMode, UserAnswers, WhichIdentificationNumbers}
import pages._
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import play.api.libs.json.Reads

sealed trait AddFIValidator {
  self: CheckYourAnswersValidator =>

  private def firstContactPhoneMissingAnswers: Seq[Page] = (userAnswers.get(FirstContactHavePhonePage) match {
    case Some(true)  => checkPage(FirstContactPhoneNumberPage)
    case Some(false) => None
    case _           => Some(FirstContactPhoneNumberPage)
  }).toSeq

  private def secContactPhoneMissingAnswers: Seq[Page] = (userAnswers.get(SecondContactCanWePhonePage) match {
    case Some(true)  => checkPage(SecondContactPhoneNumberPage)
    case Some(false) => None
    case _           => Some(SecondContactPhoneNumberPage)
  }).toSeq

  private def checkFirstContactMissingAnswers: Seq[Page] = Seq(
    checkPage(FirstContactNamePage),
    checkPage(FirstContactEmailPage)
  ).flatten ++ firstContactPhoneMissingAnswers

  private def checkSecContactDetailsMissingAnswers: Seq[Page] =
    userAnswers.get(SecondContactExistsPage) match {
      case Some(true) =>
        Seq(
          checkPage(SecondContactNamePage),
          checkPage(SecondContactEmailPage)
        ).flatten ++ secContactPhoneMissingAnswers
      case Some(false) => Seq.empty
      case _           => Seq(SecondContactExistsPage)
    }

  private[utils] def checkContactDetailsMissingAnswers = checkFirstContactMissingAnswers ++ checkSecContactDetailsMissingAnswers

  private[utils] def checkAddressMissingAnswers: Seq[Page] = any(
    checkPage(SelectedAddressLookupPage),
    checkPage(UkAddressPage)
  ).map(
    _ => PostcodePage
  ).toSeq

  private def fiGIINMissingAnswers: Seq[Page] = (userAnswers.get(HaveGIINPage) match {
    case Some(true) =>
      checkPage(WhatIsGIINPage).map(
        _ => HaveGIINPage
      )
    case Some(false) => None
    case _           => Some(HaveGIINPage)
  }).toSeq

  private def checkRegisteredBusinessName: Seq[Page] = (userAnswers.get(IsThisYourBusinessNamePage) match {
    case Some(true) => checkPage(HaveGIINPage)
    case Some(false) =>
      checkPage(NameOfFinancialInstitutionPage).map(
        _ => IsThisYourBusinessNamePage
      )
    case _ => Some(ReportForRegisteredBusinessPage)
  }).toSeq

  private def checkReportForRegisteredMissingAnswers: Seq[Page] = (userAnswers.get(ReportForRegisteredBusinessPage) match {
    case Some(true)  => checkPage(IsThisYourBusinessNamePage)
    case Some(false) => None
    case _           => Some(ReportForRegisteredBusinessPage)
  }).toSeq

  private def checkRegisteredBusinessAddress: Seq[Page] = (userAnswers.get(IsTheAddressCorrectPage) match {
    case Some(true) => None
    case Some(false) =>
      any(
        checkPage(SelectedAddressLookupPage),
        checkPage(UkAddressPage)
      ).map(
        _ => IsTheAddressCorrectPage
      )
    case _ => Some(IsTheAddressCorrectPage)
  }).toSeq

  private[utils] def checkNameIdNumbersGIINMissingAnswers: Seq[Page] = Seq(
    checkPage(NameOfFinancialInstitutionPage)
  ).flatten ++ checkIdentificationNumbersMissingAnswers ++ fiGIINMissingAnswers

  private def checkIdentificationNumbersMissingAnswers: Seq[Page] =
    userAnswers.get(WhichIdentificationNumbersPage) match {
      case Some(selectedIds) =>
        selectedIds.flatMap {
          case WhichIdentificationNumbers.UTR =>
            checkPage(WhatIsUniqueTaxpayerReferencePage).map(
              _ => WhatIsUniqueTaxpayerReferencePage
            )
          case WhichIdentificationNumbers.CRN =>
            checkPage(CompanyRegistrationNumberPage).map(
              _ => CompanyRegistrationNumberPage
            )
          case WhichIdentificationNumbers.TRN =>
            checkPage(TrustURNPage).map(
              _ => TrustURNPage
            )
        }.toSeq
      case None =>
        Seq(WhichIdentificationNumbersPage)
    }

  private[utils] def checkRegisteredBusiness: Seq[Page] = Seq(
    checkReportForRegisteredMissingAnswers ++ checkRegisteredBusinessName ++ fiGIINMissingAnswers ++ checkRegisteredBusinessAddress
  ).flatten

}

class CheckYourAnswersValidator(val userAnswers: UserAnswers) extends AddFIValidator {

  private[utils] def checkPage[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case None => Some(page)
      case _    => None
    }

  private[utils] def any(checkPages: Option[Page]*): Option[Page] = checkPages.find(_.isEmpty).getOrElse(checkPages.last)

  def validate: Seq[Page] = checkNameIdNumbersGIINMissingAnswers ++ checkAddressMissingAnswers ++ checkContactDetailsMissingAnswers

  def changeAnswersRedirectUrl: String = validate.headOption match {
    case Some(PostcodePage) =>
      controllers.addFinancialInstitution.routes.PostcodeController.onPageLoad(CheckMode).url
    case Some(FirstContactPhoneNumberPage) =>
      controllers.addFinancialInstitution.routes.FirstContactHavePhoneController.onPageLoad(CheckMode).url
    case Some(SecondContactPhoneNumberPage) =>
      controllers.addFinancialInstitution.routes.SecondContactCanWePhoneController.onPageLoad(CheckMode).url
    case Some(SecondContactEmailPage) =>
      controllers.addFinancialInstitution.routes.SecondContactEmailController.onPageLoad(CheckMode).url
    case Some(SecondContactNamePage) | Some(SecondContactExistsPage) =>
      controllers.addFinancialInstitution.routes.SecondContactExistsController.onPageLoad(CheckMode).url
    case _ =>
      controllers.addFinancialInstitution.routes.NameOfFinancialInstitutionController.onPageLoad(CheckMode).url
  }

  def validateRegisteredBusiness: Seq[Page] =
    if (userAnswers.get(IsTheAddressCorrectPage).contains(true)) checkRegisteredBusiness else checkRegisteredBusiness ++ checkAddressMissingAnswers

  def changeAnswersRedirectUrlForRegisteredBusiness: String = validateRegisteredBusiness.headOption match {
    case Some(IsTheAddressCorrectPage) =>
      controllers.addFinancialInstitution.registeredBusiness.routes.IsTheAddressCorrectController.onPageLoad(CheckMode).url
    case Some(HaveGIINPage) => controllers.addFinancialInstitution.routes.HaveGIINController.onPageLoad(CheckMode).url
    case _                  => controllers.addFinancialInstitution.registeredBusiness.routes.IsThisYourBusinessNameController.onPageLoad(CheckMode).url
  }

}

object CheckYourAnswersValidator {
  def apply(userAnswers: UserAnswers): CheckYourAnswersValidator = new CheckYourAnswersValidator(userAnswers)
}
