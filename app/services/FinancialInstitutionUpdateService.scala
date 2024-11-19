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

package services

import com.google.inject.Inject
import models.FinancialInstitutions.TINType.{GIIN, UTR}
import models.FinancialInstitutions._
import models.{GIINumber, TaxIdentificationNumber, UniqueTaxpayerReference, UserAnswers}
import pages.QuestionPage
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import play.api.libs.json.{Json, Reads}
import repositories.SessionRepository
import utils.CountryListFactory

import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionUpdateService @Inject() (
  countryListFactory: CountryListFactory,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext) {

  def populateAndSaveFiDetails(userAnswers: UserAnswers, fiDetails: FIDetail): Future[UserAnswers] =
    for {
      userAnswersWithProgressFlag <- Future.fromTry(userAnswers.set(ChangeFiDetailsInProgressId, fiDetails.FIID, cleanup = false))
      updatedUserAnswers          <- populateUserAnswersWithFiDetail(fiDetails, userAnswersWithProgressFlag)
      _                           <- sessionRepository.set(updatedUserAnswers)
    } yield updatedUserAnswers

  def populateAndSaveRegisteredFiDetails(userAnswers: UserAnswers, fiDetails: FIDetail): Future[UserAnswers] =
    for {
      userAnswersWithProgressFlag <- Future.fromTry(userAnswers.set(ChangeFiDetailsInProgressId, fiDetails.FIID, cleanup = false))
      updatedUserAnswers          <- populateUserAnswersWithRegisteredFiDetail(fiDetails, userAnswersWithProgressFlag)
      _                           <- sessionRepository.set(updatedUserAnswers)
    } yield updatedUserAnswers

  def fiDetailsHasChanged(userAnswers: UserAnswers, fiDetails: FIDetail): Boolean =
    userAnswers.get(NameOfFinancialInstitutionPage).exists(_ != fiDetails.FIName) ||
      checkTaxIdentifierForChanges(userAnswers, fiDetails.TINDetails, UTR, HaveUniqueTaxpayerReferencePage, WhatIsUniqueTaxpayerReferencePage) ||
      checkTaxIdentifierForChanges(userAnswers, fiDetails.TINDetails, GIIN, HaveGIINPage, WhatIsGIINPage) ||
      checkAddressForChanges(userAnswers, fiDetails.AddressDetails) ||
      checkPrimaryContactForChanges(userAnswers, fiDetails) ||
      checkSecondaryContactForChanges(userAnswers, fiDetails)

  def registeredFiDetailsHasChanged(userAnswers: UserAnswers, fiDetails: FIDetail): Boolean =
    userAnswers.get(NameOfFinancialInstitutionPage).exists(_ != fiDetails.FIName) ||
      checkTaxIdentifierForChanges(userAnswers, fiDetails.TINDetails, GIIN, HaveGIINPage, WhatIsGIINPage) ||
      checkAddressForChanges(userAnswers, fiDetails.AddressDetails)

  def clearUserAnswers(userAnswers: UserAnswers): Future[Boolean] =
    sessionRepository.set(userAnswers.copy(data = Json.obj()))

  private def populateUserAnswersWithFiDetail(
    fiDetails: FIDetail,
    userAnswers: UserAnswers
  )(implicit ec: ExecutionContext): Future[UserAnswers] =
    for {
      a <- Future.fromTry(userAnswers.set(NameOfFinancialInstitutionPage, fiDetails.FIName, cleanup = false))
      b <- setUTR(a, fiDetails.TINDetails)
      c <- setGIIN(b, fiDetails.TINDetails)
      d <- setAddress(c, fiDetails.AddressDetails)
      e <- setPrimaryContactDetails(d, fiDetails)
      f <- setSecondaryContactDetails(e, fiDetails)
    } yield f

  private def populateUserAnswersWithRegisteredFiDetail(
    fiDetails: FIDetail,
    userAnswers: UserAnswers
  )(implicit ec: ExecutionContext): Future[UserAnswers] =
    for {
      a <- Future.fromTry(userAnswers.set(ReportForRegisteredBusinessPage, fiDetails.IsFIUser, cleanup = false))
      b <- Future.fromTry(a.set(NameOfFinancialInstitutionPage, fiDetails.FIName, cleanup = false))
      c <- setGIIN(b, fiDetails.TINDetails)
      d <- setAddress(c, fiDetails.AddressDetails)
      e <- setFiUserDetails(d)
    } yield e

  private def setUTR(userAnswers: UserAnswers, tinDetails: Seq[TINDetails])(implicit ec: ExecutionContext): Future[UserAnswers] =
    tinDetails.find(_.TINType == UTR) match {
      case Some(details) =>
        for {
          a <- Future.fromTry(userAnswers.set(HaveUniqueTaxpayerReferencePage, true, cleanup = false))
          b <- Future.fromTry(a.set(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference(details.TIN), cleanup = false))
        } yield b
      case None =>
        for {
          a <- Future.fromTry(userAnswers.set(HaveUniqueTaxpayerReferencePage, false, cleanup = false))
          b <- Future.fromTry(a.remove(WhatIsUniqueTaxpayerReferencePage))
        } yield b
    }

  private def setGIIN(userAnswers: UserAnswers, tinDetails: Seq[TINDetails])(implicit ec: ExecutionContext): Future[UserAnswers] =
    tinDetails.find(_.TINType == GIIN) match {
      case Some(details) =>
        for {
          a <- Future.fromTry(userAnswers.set(HaveGIINPage, true, cleanup = false))
          b <- Future.fromTry(a.set(WhatIsGIINPage, GIINumber(details.TIN), cleanup = false))
        } yield b
      case None =>
        for {
          a <- Future.fromTry(userAnswers.set(HaveGIINPage, false, cleanup = false))
          b <- Future.fromTry(a.remove(WhatIsGIINPage))
        } yield b
    }

  private def setAddress(userAnswers: UserAnswers, addressDetails: AddressDetails): Future[UserAnswers] = {
    val addressPage = UkAddressPage
    for {
      a <- Future.fromTry(userAnswers.set(WhereIsFIBasedPage, true, cleanup = false))
      b <- Future.fromTry(a.set(addressPage, addressDetails.toAddress, cleanup = false))
    } yield b
  }

  private def setPrimaryContactDetails(userAnswers: UserAnswers, fiDetails: FIDetail)(implicit ec: ExecutionContext): Future[UserAnswers] = {
    val primaryContact = fiDetails.PrimaryContactDetails
    primaryContact map {
      contact =>
        for {
          a <- Future.fromTry(userAnswers.set(FirstContactNamePage, contact.ContactName, cleanup = false))
          b <- Future.fromTry(a.set(FirstContactEmailPage, contact.EmailAddress, cleanup = false))
          c <- setPhoneNumber(b, contact, FirstContactHavePhonePage, FirstContactPhoneNumberPage)
        } yield c
    } getOrElse Future.successful(userAnswers)
  }

  private def setPhoneNumber(
    userAnswers: UserAnswers,
    contactDetails: ContactDetails,
    havePhonePage: QuestionPage[Boolean],
    phoneNumberPage: QuestionPage[String]
  )(implicit ec: ExecutionContext): Future[UserAnswers] =
    contactDetails.PhoneNumber match {
      case Some(phoneNumber) =>
        for {
          a <- Future.fromTry(userAnswers.set(havePhonePage, true, cleanup = false))
          b <- Future.fromTry(a.set(phoneNumberPage, phoneNumber, cleanup = false))
        } yield b
      case None =>
        for {
          a <- Future.fromTry(userAnswers.set(havePhonePage, false, cleanup = false))
          b <- Future.fromTry(a.remove(phoneNumberPage))
        } yield b
    }

  private def setFiUserDetails(userAnswers: UserAnswers): Future[UserAnswers] = for {
    a <- Future.fromTry(userAnswers.set(ReportForRegisteredBusinessPage, true, cleanup = false))
    b <- Future.fromTry(a.set(IsThisYourBusinessNamePage, true, cleanup = false))
    c <- Future.fromTry(b.set(IsThisAddressPage, true, cleanup = false))
    d <- Future.fromTry(c.set(IsTheAddressCorrectPage, true, cleanup = false))
  } yield d

  private def setSecondaryContactDetails(userAnswers: UserAnswers, fiDetails: FIDetail)(implicit ec: ExecutionContext): Future[UserAnswers] =
    for {
      a <- Future.fromTry(userAnswers.set(SecondContactExistsPage, fiDetails.SecondaryContactDetails.isDefined, cleanup = false))
      b <- fiDetails.SecondaryContactDetails match {
        case Some(secondaryContact) =>
          for {
            b <- Future.fromTry(a.set(SecondContactNamePage, secondaryContact.ContactName, cleanup = false))
            c <- Future.fromTry(b.set(SecondContactEmailPage, secondaryContact.EmailAddress, cleanup = false))
            d <- setPhoneNumber(c, secondaryContact, SecondContactCanWePhonePage, SecondContactPhoneNumberPage)
          } yield d
        case None =>
          for {
            b <- Future.fromTry(a.remove(SecondContactNamePage))
            c <- Future.fromTry(b.remove(SecondContactEmailPage))
            d <- Future.fromTry(c.remove(SecondContactCanWePhonePage))
            e <- Future.fromTry(d.remove(SecondContactPhoneNumberPage))
          } yield e
      }
    } yield b

  private def checkTaxIdentifierForChanges[T <: TaxIdentificationNumber](
    userAnswers: UserAnswers,
    tinDetails: Seq[TINDetails],
    idType: TINType,
    haveIdTypePage: QuestionPage[Boolean],
    idTypePage: QuestionPage[T]
  )(implicit reads: Reads[T]): Boolean =
    tinDetails.find(_.TINType == idType) match {
      case Some(id) =>
        userAnswers.get(haveIdTypePage).contains(false) ||
        userAnswers.get(idTypePage).exists(_.value.toLowerCase != id.TIN.toLowerCase)
      case None =>
        userAnswers.get(haveIdTypePage).contains(true)
    }

  private def checkAddressForChanges(userAnswers: UserAnswers, addressDetails: AddressDetails): Boolean = {
    val fetchedAddress = Option(addressDetails.toAddress)
    val enteredAddress = (userAnswers.get(UkAddressPage), userAnswers.get(SelectedAddressLookupPage)) match {
      case (Some(ukAddress), None)             => Some(ukAddress)
      case (None, Some(selectedLookupAddress)) => Some(selectedLookupAddress.toAddress)
      case _                                   => None
    }

    val addressHasChanged = (fetchedAddress, enteredAddress) match {
      case (Some(a), Some(b)) => a != b
      case (None, None)       => false
      case _                  => true
    }

    addressHasChanged
  }

  private def checkPrimaryContactForChanges(userAnswers: UserAnswers, fiDetails: FIDetail): Boolean = {
    val primaryContact = fiDetails.PrimaryContactDetails
    primaryContact match {
      case Some(contact) =>
        userAnswers.get(FirstContactNamePage).exists(_ != contact.ContactName) ||
        userAnswers.get(FirstContactEmailPage).exists(_ != contact.EmailAddress) ||
        checkPhoneNumberForChanges(userAnswers, contact, FirstContactHavePhonePage, FirstContactPhoneNumberPage)
      case _ => false
    }
  }

  private def checkSecondaryContactForChanges(userAnswers: UserAnswers, fiDetails: FIDetail): Boolean =
    fiDetails.SecondaryContactDetails match {
      case Some(secondaryContact) =>
        userAnswers.get(SecondContactExistsPage).contains(false) ||
        userAnswers.get(SecondContactNamePage).exists(_ != secondaryContact.ContactName) ||
        userAnswers.get(SecondContactEmailPage).exists(_ != secondaryContact.EmailAddress) ||
        checkPhoneNumberForChanges(userAnswers, secondaryContact, SecondContactCanWePhonePage, SecondContactPhoneNumberPage)
      case None =>
        userAnswers.get(SecondContactExistsPage).contains(true)
    }

  private def checkPhoneNumberForChanges(
    userAnswers: UserAnswers,
    contactDetails: ContactDetails,
    havePhonePage: QuestionPage[Boolean],
    phoneNumberPage: QuestionPage[String]
  ): Boolean =
    contactDetails.PhoneNumber match {
      case Some(phoneNumber) =>
        userAnswers.get(havePhonePage).contains(false) ||
        userAnswers.get(phoneNumberPage).exists(_ != phoneNumber)
      case None =>
        userAnswers.get(havePhonePage).contains(true)
    }

}
