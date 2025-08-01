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

package base

import controllers.actions._
import models.FinancialInstitutions._
import models.{Address, AddressLookup, AddressResponse, Country, GIINumber, UniqueTaxpayerReference, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, PrivateMethodTester, TryValues}
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, IsTheAddressCorrectPage, IsThisYourBusinessNamePage}
import pages.addFinancialInstitution._
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.test.FakeRequest
import queries.Settable
import uk.gov.hmrc.http.HeaderCarrier

trait SpecBase extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience with PrivateMethodTester {

  val userAnswersId: String = "FATCAID"
  val fiName                = "Financial Institution"
  val fiDetailName          = "First FI"
  val testFiid              = "683373339"
  val validURN              = "ABCDEFG12345678"
  val testBusinessName      = "User Business"

  private val safeId    = "XE0000123456789"
  private val OrgName   = "Some Test Org"
  private val TestEmail = "test@test.com"

  val orgRegWithUtrResponse: String =
    s"""
       |{
       |"registerWithIDResponse": {
       |"responseCommon": {
       |"status": "OK",
       |"statusText": "Sample status text",
       |"processingDate": "2016-08-16T15:55:30Z",
       |"returnParameters": [
       |{
       |"paramName":
       |"SAP_NUMBER",
       |"paramValue":
       |"0123456789"
       |}
       |]
       |},
       |"responseDetail": {
       |"SAFEID": "$safeId",
       |"ARN": "WARN8764123",
       |"isEditable": true,
       |"isAnAgent": false,
       |"isAnIndividual": true,
       |"organisation": {
       |"organisationName": "$OrgName",
       |"isAGroup": false,
       |"organisationType": "0001"
       |},
       |"address": {
       |"addressLine1": "100 Parliament Street",
       |"addressLine4": "London",
       |"postalCode": "SW1A 2BQ",
       |"countryCode": "GB"
       |},
       |"contactDetails": {
       |"phoneNumber":
       |"1111111",
       |"mobileNumber":
       |"2222222",
       |"faxNumber":
       |"1111111",
       |"emailAddress":
       |"$TestEmail"
       |}
       |}
       |}
       |}""".stripMargin

  val testFiDetail: FIDetail =
    FIDetail(
      s"$testFiid",
      "First FI",
      "[subscriptionId]",
      List(),
      Some("689355555"),
      IsFIUser = true,
      AddressDetails("22", Some("High Street"), Some("Dawley"), Some("Dawley"), Some("GB"), Some("TF22 2RE")),
      Some(ContactDetails("Jane Doe", "janedoe@example.com", Some("0444458888"))),
      Some(ContactDetails("John Doe", "johndoe@example.com", Some("0333458888")))
    )

  val testFiDetails: Seq[FIDetail] =
    Seq(
      FIDetail(
        "683373339",
        "First FI",
        "[subscriptionId]",
        List(),
        Some("689355555"),
        IsFIUser = true,
        AddressDetails("22", Some("High Street"), Some("Dawley"), Some("Dawley"), Some("GB"), Some("TF22 2RE")),
        Some(ContactDetails("Jane Doe", "janedoe@example.com", Some("0444458888"))),
        Some(ContactDetails("John Doe", "johndoe@example.com", Some("0333458888")))
      ),
      FIDetail(
        "683373300",
        "Second FI",
        "[subscriptionId]",
        List(),
        Some("689344444"),
        IsFIUser = false,
        AddressDetails("22", Some("High Street"), Some("Dawley"), Some("Dawley"), Some("GB"), Some("TF22 2RE")),
        Some(ContactDetails("Foo Bar", "fbar@example.com", Some("0223458888"))),
        Some(ContactDetails("Foobar Baz", "fbaz@example.com", Some("0123456789")))
      )
    )

  val testViewFIDetailsBody =
    """{
    "ViewFIDetails": {
      "ResponseDetails": {
        "FIDetails": [
          {
            "FIID": "683373339",
            "FIName": "First FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [],
            "GIIN": "689355555",
            "IsFIUser": true,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "High Street",
              "AddressLine3": "Dawley",
              "AddressLine4": "Dawley",
              "CountryCode": "GB",
              "PostalCode": "TF22 2RE"
            },
            "PrimaryContactDetails": {
              "ContactName": "Jane Doe",
              "EmailAddress": "janedoe@example.com",
              "PhoneNumber": "0444458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "John Doe",
              "EmailAddress": "johndoe@example.com",
              "PhoneNumber": "0333458888"
            }
          },
          {
            "FIID": "683373300",
            "FIName": "Second FI",
            "SubscriptionID": "[subscriptionId]",
            "TINDetails": [],
            "GIIN": "689344444",
            "IsFIUser": false,
            "AddressDetails": {
              "AddressLine1": "22",
              "AddressLine2": "High Street",
              "AddressLine3": "Dawley",
              "AddressLine4": "Dawley",
              "CountryCode": "GB",
              "PostalCode": "TF22 2RE"
            },
            "PrimaryContactDetails": {
              "ContactName": "Foo Bar",
              "EmailAddress": "fbar@example.com",
              "PhoneNumber": "0223458888"
            },
            "SecondaryContactDetails": {
              "ContactName": "Foobar Baz",
              "EmailAddress": "fbaz@example.com",
              "PhoneNumber": "0123456789"
            }
          }
        ]
      }
    }
  }
"""

  val testViewFIDetailsErrorBody =
    """{
      |  "errorDetail": {
      |    "errorCode": "001",
      |    "errorMessage": "No matching records found"
      |  }
      |}
      |""".stripMargin

  val testAddress: Address                 = Address("value 1", Some("value 2"), Some("value 3"), Some("value 4"), Some("XX9 9XX"), Country.GB)
  val testAddressResponse: AddressResponse = AddressResponse("value 1", Some("value 2"), Some("value 3"), Some("value 4"), Some("XX9 9XX"), Country.GB.code)

  val testNonUKAddressResponse: AddressResponse =
    AddressResponse("value 1", Some("value 2"), Some("value 3"), Some("value 4"), Some("XX9 9XX"), "DE")

  val testAddressLookup: AddressLookup = AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ", Some(Country.GB))

  val userAnswersForAddFI: UserAnswers = emptyUserAnswers
    .withPage(NameOfFinancialInstitutionPage, "testfi")
    .withPage(WhichIdentificationNumbersPage, Set[TINType](TINType.UTR))
    .withPage(WhatIsUniqueTaxpayerReferencePage, UniqueTaxpayerReference("222333444"))
    .withPage(HaveGIINPage, true)
    .withPage(WhatIsGIINPage, GIINumber("98096B.00000.LE.350"))
    .withPage(SelectedAddressLookupPage, testAddressLookup)
    .withPage(IsThisAddressPage, true)
    .withPage(FirstContactNamePage, "MrTest")
    .withPage(FirstContactEmailPage, "MrTest@test.com")
    .withPage(FirstContactHavePhonePage, true)
    .withPage(FirstContactPhoneNumberPage, "0123456789")
    .withPage(SecondContactExistsPage, true)
    .withPage(SecondContactNamePage, "MrsTest")
    .withPage(SecondContactEmailPage, "MrsTest@test.com")
    .withPage(SecondContactCanWePhonePage, true)
    .withPage(SecondContactPhoneNumberPage, "0234567891")

  val userAnswersForAddUserAsFI: UserAnswers = emptyUserAnswers
    .withPage(IsThisYourBusinessNamePage, true)
    .withPage(HaveGIINPage, true)
    .withPage(WhatIsGIINPage, GIINumber("98096B.00000.LE.350"))
    .withPage(IsTheAddressCorrectPage, true)
    .withPage(FetchedRegisteredAddressPage, testAddressResponse)

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  implicit class UserAnswersExtension(userAnswers: UserAnswers) {

    def withPage[T](page: Settable[T], value: T)(implicit writes: Writes[T]): UserAnswers =
      userAnswers.set(page, value, cleanup = false).success.value

    def removePage[T](page: Settable[T]): UserAnswers = userAnswers.remove(page).success.value

  }

}
