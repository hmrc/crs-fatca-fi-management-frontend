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
import models.FinancialInstitutions.TINType._
import models.FinancialInstitutions._
import models.requests.DataRequest
import models.{AddressLookup, AddressResponse, CompanyRegistrationNumber, Country, GIINumber, TrustUniqueReferenceNumber, UniqueTaxpayerReference, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.{CompanyRegistrationNumberPage, TrustURNPage}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import repositories.{ChangeUserAnswersRepository, SessionRepository}
import uk.gov.hmrc.http.HeaderCarrier
import utils.CountryListFactory

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionUpdateServiceSpec extends SpecBase with MockitoSugar with UserAnswersGenerator with BeforeAndAfterEach {

  private val mockRegService: RegistrationWithUtrService = mock[RegistrationWithUtrService]
  private val mockCountryListFactory                     = mock[CountryListFactory]
  private val mockSessionRepository                      = mock[SessionRepository]
  private val mockChangeUserAnswersRepository            = mock[ChangeUserAnswersRepository]

  val utr                                       = Option(UniqueTaxpayerReference("1112223330"))
  implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "testUser", "testFatca", emptyUserAnswers, utr)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCountryListFactory, mockSessionRepository)
  }

  private val service = new FinancialInstitutionUpdateService(mockCountryListFactory, mockSessionRepository, mockChangeUserAnswersRepository, mockRegService)

  private val nonUkCountry = Country("valid", "AX", "Aland Islands")

  "FinancialInstitutionUpdateService" - {

    val persistenceError = new Exception("Failed to save user answers")
    when(mockChangeUserAnswersRepository.get(any)).thenReturn(Future.successful(None))

    def setUpMock(country: Country, ukCountryCodes: Set[String]) = {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockCountryListFactory.findCountryWithCode(any())).thenReturn(Option(country))
      when(mockCountryListFactory.countryCodesForUkCountries).thenReturn(ukCountryCodes)
    }

    "populateAndSaveFiDetails" - {

      "must populate and persist user answers with FI details" in {
        forAll {
          (fiDetails: FIDetail, isUkAddress: Boolean) =>
            val country        = if (isUkAddress) Country.GB else nonUkCountry
            val ukCountryCodes = if (isUkAddress) Set(country.code, fiDetails.AddressDetails.CountryCode.value) else Set.empty[String]
            setUpMock(country, ukCountryCodes)

            val (populatedUserAnswers, _) = service.populateAndSaveFiDetails(emptyUserAnswers, fiDetails).futureValue

            verify(mockSessionRepository, times(1)).set(populatedUserAnswers)
            verifyUserAnswersMatchFIDetails(fiDetails, populatedUserAnswers, isUkAddress)
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

    "populateAndSaveRegisteredFiDetails" - {

      val fiDetails       = testFiDetail
      val testAddressResp = AddressResponse("22", Some("High Street"), Some("Dawley"), Some("Dawley"), Some("TF22 2RE"), "GB")
      val country         = Country.GB
      val ukCountryCodes  = Set(country.code, fiDetails.AddressDetails.CountryCode.value)

      "must populate and persist user answers with Reg FI details with different Reg Address" in {
        setUpMock(country, ukCountryCodes)
        when(mockRegService.fetchAddress(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(testAddressResp))

        val (populatedUserAnswers, _) = service.populateAndSaveRegisteredFiDetails(emptyUserAnswers, fiDetails)(request, hc).futureValue

        populatedUserAnswers.get(ReportForRegisteredBusinessPage) mustBe Some(true)
        populatedUserAnswers.get(IsTheAddressCorrectPage) mustBe Some(true)
        populatedUserAnswers.get(IsThisYourBusinessNamePage) mustBe Some(true)

        verify(mockSessionRepository, times(1)).set(populatedUserAnswers)
        verify(mockRegService, times(1)).fetchAddress(utr.get)
      }

      "must populate and persist user answers with Reg FI details with different address" in {
        setUpMock(country, ukCountryCodes)
        when(mockRegService.fetchAddress(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(testAddressResponse))

        val (populatedUserAnswers, _) = service.populateAndSaveRegisteredFiDetails(emptyUserAnswers, fiDetails)(request, hc).futureValue

        populatedUserAnswers.get(IsTheAddressCorrectPage) mustBe Some(false)
      }

      "must return error when there is a failure while retrieving the registered address" in {
        setUpMock(country, ukCountryCodes)
        when(mockRegService.fetchAddress(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.failed(new RuntimeException("Failed to get")))

        an[RuntimeException] must be thrownBy service.populateAndSaveRegisteredFiDetails(emptyUserAnswers, fiDetails).futureValue
      }

      "must return error when there is a failure while persisting the user answers" in {
        forAll {
          fiDetails: FIDetail =>
            when(mockSessionRepository.set(any())).thenReturn(Future.failed(persistenceError))

            an[Exception] must be thrownBy service.populateAndSaveRegisteredFiDetails(emptyUserAnswers, fiDetails).futureValue
        }
      }
    }

    "fiDetailsHasChanged" - {

      "must return false when there has been no changes to the FI details" in {
        forAll {
          fiDetails: FIDetail =>
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (populatedUserAnswers, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe false
        }
      }

      "must return true when the NameOfFinancialInstitutionPage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(NameOfFinancialInstitutionPage, UUID.randomUUID() + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the UkAddressPage in user answers does not match address in FIDetail" in {
        forAll {
          (fiDetails: FIDetail, newAddress: AddressDetails) =>
            setUpMock(Country.GB, Set(fiDetails.AddressDetails.CountryCode.value, newAddress.CountryCode.value))

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .removePage(NonUkAddressPage)
              .removePage(SelectedAddressLookupPage)
              .withPage(UkAddressPage, newAddress.toAddress(mockCountryListFactory).value)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the NonUkAddressPage in user answers does not match address in FIDetail" in {
        forAll {
          (fiDetails: FIDetail, newAddress: AddressDetails) =>
            setUpMock(nonUkCountry, Set(fiDetails.AddressDetails.CountryCode.value, newAddress.CountryCode.value))

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .removePage(UkAddressPage)
              .removePage(SelectedAddressLookupPage)
              .withPage(NonUkAddressPage, newAddress.toAddress(mockCountryListFactory).value)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the SelectedAddressLookupPage in user answers does not match address in FIDetail" in {
        forAll {
          (fiDetails: FIDetail, newAddress: AddressLookup) =>
            setUpMock(Country.GB, Set(fiDetails.AddressDetails.CountryCode.value, Country.GB.code))

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .removePage(UkAddressPage)
              .removePage(NonUkAddressPage)
              .withPage(SelectedAddressLookupPage, newAddress)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the FirstContactNamePage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(FirstContactNamePage, UUID.randomUUID() + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when the FirstContactEmailPage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(FirstContactEmailPage, UUID.randomUUID() + "suffix")

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when FirstContactHavePhonePage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(FirstContactHavePhonePage, fiDetails.PrimaryContactDetails.map(_.PhoneNumber).isEmpty)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when SecondContactExistsPage in user answers does not equal value in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(SecondContactExistsPage, fiDetails.SecondaryContactDetails.isEmpty)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when user answers UTR is different from UTR in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            val fiDetailsUTR = UniqueTaxpayerReference(UUID.randomUUID().toString)
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(WhichIdentificationNumbersPage, Set[TINType](UTR))
              .withPage(WhatIsUniqueTaxpayerReferencePage, fiDetailsUTR)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetails) mustBe true
        }
      }

      "must return true when user answers GIIN is different from GIIN in FIDetail" in {
        forAll {
          fiDetails: FIDetail =>
            val fiDetailsGIIN     = GIINumber(UUID.randomUUID().toString)
            val fiDetailsWithGIIN = fiDetails.copy(TINDetails = Seq(TINDetails(TINType = UTR, TIN = fiDetailsGIIN.value, "")))
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(WhatIsGIINPage, GIINumber(UUID.randomUUID().toString))

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithGIIN) mustBe true
        }
      }

      "must return true when FirstContactHavePhonePage is false but there is a phone in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String, phone: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(PrimaryContactDetails = Some(ContactDetails(name, email, PhoneNumber = Option(phone))))
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(FirstContactHavePhonePage, false)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when FirstContactHavePhonePage is true but there is no first contact phone in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(PrimaryContactDetails = Some(ContactDetails(name, email, PhoneNumber = None)))
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
              .withPage(FirstContactHavePhonePage, true)

            service.fiDetailsHasChanged(populatedUserAnswers, fiDetailsWithSecondaryContact) mustBe true
        }
      }

      "must return true when SecondContactNamePage is different from that in FIDetails" in {
        forAll {
          (fiDetails: FIDetail, name: String, email: String) =>
            val fiDetailsWithSecondaryContact = fiDetails
              .copy(SecondaryContactDetails = Option(ContactDetails(name, email, PhoneNumber = None)))
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
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
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
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
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
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
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
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
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
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
            setUpMock(Country.GB, fiDetails.AddressDetails.CountryCode.toSet)

            val (result, _) = service
              .populateAndSaveFiDetails(emptyUserAnswers, fiDetails)
              .futureValue

            val populatedUserAnswers = result
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

  private def verifyUserAnswersMatchFIDetails(fiDetails: FIDetail, populatedUserAnswers: UserAnswers, isUkAddress: Boolean) = {
    populatedUserAnswers.get(NameOfFinancialInstitutionPage).value mustBe fiDetails.FIName

    verifyTINMatch(fiDetails, populatedUserAnswers)

    val addressPage = if (isUkAddress) UkAddressPage else NonUkAddressPage
    populatedUserAnswers.get(addressPage).value mustBe fiDetails.AddressDetails.toAddress(mockCountryListFactory).value

    populatedUserAnswers.get(FirstContactNamePage).value mustBe fiDetails.PrimaryContactDetails.map(_.ContactName).get
    populatedUserAnswers.get(FirstContactEmailPage).value mustBe fiDetails.PrimaryContactDetails.map(_.EmailAddress).get
    populatedUserAnswers.get(FirstContactHavePhonePage).value mustBe fiDetails.PrimaryContactDetails.map(_.PhoneNumber).isDefined
    populatedUserAnswers.get(FirstContactPhoneNumberPage) mustBe fiDetails.PrimaryContactDetails.map(_.PhoneNumber).get

    populatedUserAnswers.get(SecondContactExistsPage).value mustBe fiDetails.SecondaryContactDetails.isDefined
    populatedUserAnswers.get(SecondContactNamePage) mustBe fiDetails.SecondaryContactDetails.map(_.ContactName)
    populatedUserAnswers.get(SecondContactEmailPage) mustBe fiDetails.SecondaryContactDetails.map(_.EmailAddress)
    populatedUserAnswers.get(SecondContactCanWePhonePage) mustBe fiDetails.SecondaryContactDetails.map(_.PhoneNumber.isDefined)
    populatedUserAnswers.get(SecondContactPhoneNumberPage) mustBe fiDetails.SecondaryContactDetails.flatMap(_.PhoneNumber)
  }

  private def verifyTINMatch(fiDetails: FIDetail, populatedUserAnswers: UserAnswers) = {
    val maybeUTR: Option[TINDetails] = fiDetails.TINDetails.find(_.TINType == UTR)
    populatedUserAnswers.get(WhichIdentificationNumbersPage) contains UTR
    populatedUserAnswers.get(WhatIsUniqueTaxpayerReferencePage) mustBe maybeUTR.map(
      id => UniqueTaxpayerReference(id.TIN)
    )
    val maybeCRN: Option[TINDetails] = fiDetails.TINDetails.find(_.TINType == CRN)
    populatedUserAnswers.get(WhichIdentificationNumbersPage) contains CRN
    populatedUserAnswers.get(CompanyRegistrationNumberPage) mustBe maybeCRN.map(
      id => CompanyRegistrationNumber(id.TIN)
    )

    val maybeTRN: Option[TINDetails] = fiDetails.TINDetails.find(_.TINType == TURN)
    populatedUserAnswers.get(WhichIdentificationNumbersPage) contains TURN
    populatedUserAnswers.get(TrustURNPage) mustBe maybeTRN.map(
      id => TrustUniqueReferenceNumber(id.TIN)
    )
  }

}
