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

import base.SpecBase
import generators.UserAnswersGenerator
import models.FinancialInstitutions.{ContactDetails, FIDetail, TINDetails}
import models.FinancialInstitutions.TINType.{GIIN, UTR}
import models.{AddressResponse, GIINumber, UniqueTaxpayerReference, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.addFinancialInstitution.IsRegisteredBusiness.FetchedRegisteredAddressPage
import pages.addFinancialInstitution._
import play.api.libs.json.Json
import repositories.SessionRepository

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialInstitutionUpdateServiceSpec extends SpecBase with MockitoSugar with UserAnswersGenerator with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  private val service = new FinancialInstitutionUpdateService(mockSessionRepository)

  "FinancialInstitutionUpdateService" - {

    val persistenceError = new Exception("Failed to save user answers")

    "populateAndSaveFiDetails" - {

      "must populate and persist user answers with FI details" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service.populateAndSaveFiDetails(emptyUserAnswers, fiDetails).futureValue

            verify(mockSessionRepository, times(1)).set(populatedUserAnswers)
            verifyUserAnswersMatchFIDetails(fiDetails, populatedUserAnswers)
        }
      }

      "must return error when there is a failure while persisting the user answers" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.failed(persistenceError))

            an[Exception] must be thrownBy service.populateAndSaveFiDetails(emptyUserAnswers, fiDetails).futureValue
        }
      }
    }

    "fiDetailsHasChanged" - {

      "must return false when there has been no changes to the FI details" in {
        forAll {
          fiDetails: FIDetail =>
            val populatedUserAnswers = emptyUserAnswers

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe false
        }
      }

      "must return true when the NameOfFinancialInstitutionPage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(NameOfFinancialInstitutionPage, UUID.randomUUID() + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the FirstContactNamePage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(FirstContactNamePage, UUID.randomUUID() + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the FirstContactEmailPage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(FirstContactEmailPage, UUID.randomUUID() + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when FirstContactHavePhonePage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(FirstContactHavePhonePage, fiDetails.PrimaryContactDetails.PhoneNumber.isEmpty)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when SecondContactExistsPage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, fiDetails.SecondaryContactDetails.isEmpty)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when user answers UTR is different from UTR in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            val fiDetailsUTR     = UniqueTaxpayerReference(UUID.randomUUID().toString)
            val fiDetailsWithUTR = fiDetails.copy(TINDetails = Seq(TINDetails(TINType = UTR, TIN = fiDetailsUTR.value, "")))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetailsWithUTR)
              .futureValue
              .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference(UUID.randomUUID().toString))

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithUTR) mustBe true
        }
      }

      "must return true when user answers GIIN is different from GIIN in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            val fiDetailsGIIN     = GIINumber(UUID.randomUUID().toString)
            val fiDetailsWithGIIN = fiDetails.copy(TINDetails = Seq(TINDetails(TINType = GIIN, TIN = fiDetailsGIIN.value, "")))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetailsWithGIIN)
              .futureValue
              .withPage(WhatIsGIINPage, GIINumber(UUID.randomUUID().toString))

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithGIIN) mustBe true
        }
      }

      "must return true when FirstContactHavePhonePage is false but there is a phone in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String, phone: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(PrimaryContactDetails = ContactDetails(name, email, PhoneNumber = Option(phone)))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(FirstContactHavePhonePage, false)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when FirstContactHavePhonePage is true but there is no first contact phone in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(PrimaryContactDetails = ContactDetails(name, email, PhoneNumber = None))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(FirstContactHavePhonePage, true)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when SecondContactNamePage is different from that in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = None)))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, true)
              .withPage(SecondContactNamePage, UUID.randomUUID().toString + "suffix")
              .withPage(SecondContactEmailPage, email)
              .withPage(SecondContactCanWePhonePage, false)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when SecondContactEmailPage is different from that in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = None)))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, true)
              .withPage(SecondContactNamePage, name)
              .withPage(SecondContactEmailPage, UUID.randomUUID().toString + "suffix")
              .withPage(SecondContactCanWePhonePage, false)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when SecondContactPhoneNumberPage is different from second contact phone in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String, phone: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = Option(phone))))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, true)
              .withPage(SecondContactNamePage, name)
              .withPage(SecondContactEmailPage, email)
              .withPage(SecondContactCanWePhonePage, true)
              .withPage(SecondContactPhoneNumberPage, UUID.randomUUID().toString + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when user answers SecondContactExistsPage is false but second contact exists in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = None)))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, false)
              .withPage(SecondContactNamePage, name)
              .withPage(SecondContactEmailPage, email)
              .withPage(SecondContactCanWePhonePage, false)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when user answers SecondContactCanWePhonePage is true but second contact phone does not exists in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = None)))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, true)
              .withPage(SecondContactNamePage, name)
              .withPage(SecondContactEmailPage, email)
              .withPage(SecondContactCanWePhonePage, true)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when user answers SecondContactCanWePhonePage is false but second contact phone exists in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String, phone: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = Option(phone))))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val populatedUserAnswers = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue
              .withPage(SecondContactExistsPage, true)
              .withPage(SecondContactNamePage, name)
              .withPage(SecondContactEmailPage, email)
              .withPage(SecondContactCanWePhonePage, false)
              .withPage(SecondContactPhoneNumberPage, phone)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }
    }

    "clearUserAnswers" - {

      "must clear user answers data" in {
        forAll {
          userAnswers: UserAnswers =>
            val userAnswersWithDataCleared = userAnswers.copy(data = Json.obj())

            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            service.clearUserAnswers(userAnswers).futureValue

            verify(mockSessionRepository, times(1)).set(userAnswersWithDataCleared)
        }
      }

      "must return error when there is a failure while persisting the user answers" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.failed(persistenceError))

        an[Exception] must be thrownBy service.clearUserAnswers(emptyUserAnswers).futureValue
      }
    }
  }

  private def verifyUserAnswersMatchFIDetails(fiDetails: FIDetail, populatedUserAnswers: UserAnswers) = {
    populatedUserAnswers.get(NameOfFinancialInstitutionPage).value mustBe fiDetails.FIName

    val maybeUTR = fiDetails.TINDetails.find(_.TINType == UTR)
    populatedUserAnswers.get(HaveUniqueTaxpayerReferencePage).value mustBe maybeUTR.isDefined
    populatedUserAnswers.get(WhatIsUniqueTaxpayerReferencePage) mustBe maybeUTR.map(
      id => UniqueTaxpayerReference(id.TIN)
    )

    val maybeGIIN = fiDetails.TINDetails.find(_.TINType == GIIN)
    populatedUserAnswers.get(HaveGIINPage).value mustBe maybeGIIN.isDefined
    populatedUserAnswers.get(WhatIsGIINPage) mustBe maybeGIIN.map(
      id => GIINumber(id.TIN)
    )

    populatedUserAnswers.get(FetchedRegisteredAddressPage).value mustBe AddressResponse.fromAddressDetails(fiDetails.AddressDetails)

    populatedUserAnswers.get(FirstContactNamePage).value mustBe fiDetails.PrimaryContactDetails.ContactName
    populatedUserAnswers.get(FirstContactEmailPage).value mustBe fiDetails.PrimaryContactDetails.EmailAddress
    populatedUserAnswers.get(FirstContactHavePhonePage).value mustBe fiDetails.PrimaryContactDetails.PhoneNumber.isDefined
    populatedUserAnswers.get(FirstContactPhoneNumberPage) mustBe fiDetails.PrimaryContactDetails.PhoneNumber

    populatedUserAnswers.get(SecondContactExistsPage).value mustBe fiDetails.SecondaryContactDetails.isDefined
    populatedUserAnswers.get(SecondContactNamePage) mustBe fiDetails.SecondaryContactDetails.map(_.ContactName)
    populatedUserAnswers.get(SecondContactEmailPage) mustBe fiDetails.SecondaryContactDetails.map(_.EmailAddress)
    populatedUserAnswers.get(SecondContactCanWePhonePage) mustBe fiDetails.SecondaryContactDetails.map(_.PhoneNumber.isDefined)
    populatedUserAnswers.get(SecondContactPhoneNumberPage) mustBe fiDetails.SecondaryContactDetails.flatMap(_.PhoneNumber)
  }

}
