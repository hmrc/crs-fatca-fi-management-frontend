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

import models.UserAnswers
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

  private[utils] def checkAddressMissingAnswers: Seq[Page] = (userAnswers.get(WhereIsFIBasedPage) match {
    case Some(true) =>
      checkPage(PostcodePage)
        .orElse(
          any(
            checkPage(SelectedAddressLookupPage),
            checkPage(UkAddressPage),
            checkPage(IsThisAddressPage)
          ).map(
            _ => PostcodePage
          )
        )
    case Some(false) => checkPage(NonUkAddressPage)
    case _           => Some(WhereIsFIBasedPage)
  }).toSeq

  private def fiUTRMissingAnswers: Seq[Page] = (userAnswers.get(HaveUniqueTaxpayerReferencePage) match {
    case Some(true)  => checkPage(WhatIsUniqueTaxpayerReferencePage)
    case Some(false) => None
    case _           => Some(HaveUniqueTaxpayerReferencePage)
  }).toSeq

  private def fiGIINMissingAnswers: Seq[Page] = (userAnswers.get(HaveGIINPage) match {
    case Some(true)  => checkPage(WhatIsGIINPage)
    case Some(false) => None
    case _           => Some(HaveGIINPage)
  }).toSeq

  private def checkRegisteredBusinessName: Seq[Page] = (userAnswers.get(IsThisYourBusinessNamePage) match {
    case Some(true)  => checkPage(HaveGIINPage)
    case Some(false) => checkPage(NameOfFinancialInstitutionPage)
    case _           => Some(ReportForRegisteredBusinessPage)
  }).toSeq

  private def checkReportForRegisteredMissingAnswers: Seq[Page] = (userAnswers.get(ReportForRegisteredBusinessPage) match {
    case Some(true)  => checkPage(IsThisYourBusinessNamePage)
    case Some(false) => None
    case _           => Some(ReportForRegisteredBusinessPage)
  }).toSeq

  private def checkRegisteredBusinessAddress: Seq[Page] = (userAnswers.get(IsTheAddressCorrectPage) match {
    case Some(true)  => None
    case Some(false) => checkPage(WhereIsFIBasedPage)
    case _           => Some(IsTheAddressCorrectPage)
  }).toSeq

  private[utils] def checkNameUTRGIINMissingAnswers: Seq[Page] = Seq(
    checkPage(NameOfFinancialInstitutionPage)
  ).flatten ++ fiUTRMissingAnswers ++ fiGIINMissingAnswers

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

  def validate: Seq[Page] = checkNameUTRGIINMissingAnswers ++ checkAddressMissingAnswers ++ checkContactDetailsMissingAnswers

  def validateRegisteredBusiness: Seq[Page] =
    if (userAnswers.get(IsTheAddressCorrectPage).contains(true)) checkRegisteredBusiness else checkRegisteredBusiness ++ checkAddressMissingAnswers

}

object CheckYourAnswersValidator {
  def apply(userAnswers: UserAnswers): CheckYourAnswersValidator = new CheckYourAnswersValidator(userAnswers)
}
