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
import models.FinancialInstitutions.TINType._
import models.FinancialInstitutions._
import models.{CompanyRegistrationNumber, GIINumber, UniqueTaxpayerReference, UserAnswers}
import pages.addFinancialInstitution.IsRegisteredBusiness.{
  FetchedRegisteredAddressPage,
  IsTheAddressCorrectPage,
  IsThisYourBusinessNamePage,
  ReportForRegisteredBusinessPage
}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import pages.{CompanyRegistrationNumberPage, QuestionPage, TrustURNPage}
import play.api.libs.json.Json
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import utils.CountryListFactory

import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionUpdateService @Inject() (
  countryListFactory: CountryListFactory,
  sessionRepository: SessionRepository,
  changeUserAnswersRepository: ChangeUserAnswersRepository
)(implicit ec: ExecutionContext) {

  def populateAndSaveFiDetails(userAnswers: UserAnswers, fiDetails: FIDetail): Future[UserAnswers] = for {
    userAnswersWithProgressFlag <- Future.fromTry(userAnswers.set(ChangeFiDetailsInProgressId, fiDetails.FIID, cleanup = false))
    changeId = s"${fiDetails.SubscriptionID}-${fiDetails.FIID}"
    changeAnswers      <- changeUserAnswersRepository.get(changeId).map(_.map(_.copy(id = userAnswers.id)))
    updatedUserAnswers <- changeAnswers.fold(populateUserAnswersWithFiDetail(fiDetails, userAnswersWithProgressFlag))(Future.successful)
    _                  <- sessionRepository.set(updatedUserAnswers)
  } yield updatedUserAnswers

  def populateAndSaveRegisteredFiDetails(userAnswers: UserAnswers, fiDetails: FIDetail): Future[UserAnswers] =
    for {
      userAnswersWithProgressFlag <- Future.fromTry(userAnswers.set(ChangeFiDetailsInProgressId, fiDetails.FIID, cleanup = false))
      changeId = s"${fiDetails.SubscriptionID}-${fiDetails.FIID}"
      changeAnswers      <- changeUserAnswersRepository.get(changeId).map(_.map(_.copy(id = userAnswers.id)))
      updatedUserAnswers <- changeAnswers.fold(populateUserAnswersWithRegisteredFiDetail(fiDetails, userAnswersWithProgressFlag))(Future.successful)
      _                  <- sessionRepository.set(updatedUserAnswers)
    } yield updatedUserAnswers

  def fiDetailsHasChanged(userAnswers: UserAnswers, fiDetails: FIDetail): Boolean =
    userAnswers.get(NameOfFinancialInstitutionPage).exists(_ != fiDetails.FIName) ||
      checkTINTypeForChanges(userAnswers, fiDetails.TINDetails) ||
      checkAddressForChanges(userAnswers, fiDetails.AddressDetails) ||
      checkPrimaryContactForChanges(userAnswers, fiDetails) ||
      checkSecondaryContactForChanges(userAnswers, fiDetails)

  def registeredFiDetailsHasChanged(userAnswers: UserAnswers, fiDetails: FIDetail): Boolean =
    userAnswers.get(NameOfFinancialInstitutionPage).exists(_ != fiDetails.FIName) ||
      checkTINTypeForChanges(userAnswers, fiDetails.TINDetails) ||
      checkAddressForChanges(userAnswers, fiDetails.AddressDetails)

  def clearUserAnswers(userAnswers: UserAnswers): Future[Boolean] =
    sessionRepository.set(userAnswers.copy(data = Json.obj()))

  private def populateUserAnswersWithFiDetail(
    fiDetails: FIDetail,
    userAnswers: UserAnswers
  )(implicit ec: ExecutionContext): Future[UserAnswers] =
    for {
      a <- Future.fromTry(userAnswers.set(NameOfFinancialInstitutionPage, fiDetails.FIName, cleanup = false))
      b <- setTaxIdentifier(a, fiDetails.TINDetails)
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
      c <- setTaxIdentifier(b, fiDetails.TINDetails)
      d <- setGIIN(c, fiDetails.TINDetails)
      e <- setAddress(d, fiDetails.AddressDetails)
      f <- setFiUserDetails(e)
    } yield f

  private def setTaxIdentifier(userAnswers: UserAnswers, listOfTinDetails: Seq[TINDetails])(implicit ec: ExecutionContext): Future[UserAnswers] =
    listOfTinDetails.foldLeft(Future.successful(userAnswers)) {
      (futureAnswers, details) =>
        futureAnswers.flatMap {
          answers =>
            details.TINType match {
              case UTR =>
                Future.fromTry(
                  answers
                    .set(WhichIdentificationNumbersPage, answers.get(WhichIdentificationNumbersPage).getOrElse(Set.empty) + TINType.UTR, cleanup = false)
                    .flatMap(_.set(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference(details.TIN), cleanup = false))
                )
              case CRN =>
                Future.fromTry(
                  answers
                    .set(WhichIdentificationNumbersPage, answers.get(WhichIdentificationNumbersPage).getOrElse(Set.empty) + TINType.CRN, cleanup = false)
                    .flatMap(_.set(CompanyRegistrationNumberPage, CompanyRegistrationNumber(details.TIN), cleanup = false))
                )
              case TRN =>
                Future.fromTry(
                  answers
                    .set(WhichIdentificationNumbersPage, answers.get(WhichIdentificationNumbersPage).getOrElse(Set.empty) + TINType.TRN, cleanup = false)
                    .flatMap(_.set(TrustURNPage, details.TIN, cleanup = false))
                )
              case _ =>
                Future.fromTry(
                  answers
                    .set(HaveGIINPage, true, cleanup = false)
                    .flatMap(_.set(WhatIsGIINPage, GIINumber(details.TIN), cleanup = false))
                )
            }
        }
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
    val isUkAddress = addressDetails.CountryCode.exists(countryListFactory.countryCodesForUkCountries.contains)
    val addressPage = if (isUkAddress) UkAddressPage else NonUkAddressPage
    for {
      a <- addressDetails.toAddress(countryListFactory) match {
        case Some(address) => Future.fromTry(userAnswers.set(addressPage, address, cleanup = false))
        case None =>
          Future.failed(new RuntimeException(s"Failed to find country with code ${addressDetails.CountryCode}"))
      }
    } yield a
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

  private def setFiUserDetails(userAnswers: UserAnswers): Future[UserAnswers] =
    for {
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

  private def checkGIINForChanges(
    userAnswers: UserAnswers,
    tinDetails: Seq[TINDetails]
  ): Boolean =
    tinDetails.find(_.TINType == GIIN) match {
      case Some(id) =>
        userAnswers.get(HaveGIINPage).contains(false) ||
        userAnswers.get(WhatIsGIINPage).exists(_.value.toLowerCase != id.TIN.toLowerCase)
      case None =>
        userAnswers.get(HaveGIINPage).contains(true)
    }

  def checkUTRforChange(userAnswers: UserAnswers, tinDetails: Seq[TINDetails]): Boolean = {
    val uaValue: Option[String]     = userAnswers.get(WhatIsUniqueTaxpayerReferencePage).map(_.value)
    val detailValue: Option[String] = tinDetails.find(_.TINType == UTR).map(_.TIN)
    uaValue != detailValue
  }

  def checkCRNforChange(userAnswers: UserAnswers, tinDetails: Seq[TINDetails]): Boolean = {
    val uaValue: Option[String]     = userAnswers.get(CompanyRegistrationNumberPage).map(_.value)
    val detailValue: Option[String] = tinDetails.find(_.TINType == CRN).map(_.TIN)
    uaValue != detailValue
  }

  def checkTRNforChange(userAnswers: UserAnswers, tinDetails: Seq[TINDetails]): Boolean = {
    val uaValue: Option[String]     = userAnswers.get(TrustURNPage)
    val detailValue: Option[String] = tinDetails.find(_.TINType == TRN).map(_.TIN)
    uaValue != detailValue
  }

  private def checkTINTypeForChanges(
    userAnswers: UserAnswers,
    tinDetails: Seq[TINDetails]
  ): Boolean = {

    val uaTinTypes: Set[TINType] =
      userAnswers
        .get(WhichIdentificationNumbersPage)
        .getOrElse(Set.empty) ++
        (if (userAnswers.get(HaveGIINPage).contains(true)) Set(GIIN) else Set.empty)

    val detailTinTypes: Set[TINType]    = tinDetails.map(_.TINType).toSet
    val identifiersHaveChanged: Boolean = uaTinTypes != detailTinTypes
    val valuesHaveChanged: Boolean = if (!identifiersHaveChanged) {
      detailTinTypes.toSeq.exists {
        tinType =>
          tinType match {
            case TINType.UTR  => checkUTRforChange(userAnswers, tinDetails)
            case TINType.CRN  => checkCRNforChange(userAnswers, tinDetails)
            case TINType.TRN  => checkTRNforChange(userAnswers, tinDetails)
            case TINType.GIIN => checkGIINForChanges(userAnswers, tinDetails)
            case _            => false
          }
      }
    } else {
      false
    }
    identifiersHaveChanged || valuesHaveChanged
  }

  private def checkAddressForChanges(userAnswers: UserAnswers, addressDetails: AddressDetails): Boolean = {
    val isUkAddress    = addressDetails.CountryCode.exists(countryListFactory.countryCodesForUkCountries.contains)
    val fetchedAddress = addressDetails.toAddress(countryListFactory)
    val enteredAddress = if (isUkAddress) {
      (userAnswers.get(UkAddressPage), userAnswers.get(SelectedAddressLookupPage), userAnswers.get(FetchedRegisteredAddressPage)) match {
        case (Some(ukAddress), None, _)             => Option(ukAddress)
        case (None, Some(selectedLookupAddress), _) => Option(selectedLookupAddress.toAddress)
        case (None, None, Some(fetchedAddress))     => Option(fetchedAddress.toAddress)
        case _                                      => None
      }
    } else {
      userAnswers.get(NonUkAddressPage)
    }

    (fetchedAddress, enteredAddress) match {
      case (Some(a), Some(b)) => a != b
      case (None, None)       => false
      case _                  => true
    }
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
