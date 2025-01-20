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

import connectors.FinancialInstitutionsConnector
import models.FinancialInstitutions.TINType.{CRN, GIIN, TRN, UTR}
import models.FinancialInstitutions._
import models.UserAnswers
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import pages.{CompanyRegistrationNumberPage, TrustURNPage}
import play.api.libs.json.{JsResult, JsResultException, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsService @Inject() (connector: FinancialInstitutionsConnector) {

  def getListOfFinancialInstitutions(subscriptionId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[FIDetail]] =
    connector
      .viewFis(subscriptionId)
      .map(
        res => extractList(res.body)
      )

  def getFinancialInstitution(subscriptionId: String, fiId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[FIDetail]] =
    connector
      .viewFi(subscriptionId, fiId)
      .map(
        res => extractList(res.body).headOption
      )

  def getInstitutionById(details: Seq[FIDetail], fiid: String): Option[FIDetail] =
    details
      .find(
        detail => detail.FIID == fiid
      )
      .fold[Option[FIDetail]](None)(Some(_))

  def extractList(body: String): Seq[FIDetail] = {
    val json: JsValue                        = Json.parse(body)
    val listsResult: JsResult[Seq[FIDetail]] = (json \ "ViewFIDetails" \ "ResponseDetails" \ "FIDetails").validate[Seq[FIDetail]]

    listsResult.fold(
      errors => throw JsResultException(errors),
      value => value
    )
  }

  def updateFinancialInstitution(subscriptionId: String, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] = {
    val fiDetailsRequest = buildUpdateFiDetailsRequest(subscriptionId, userAnswers)
    connector
      .addOrUpdateFI(fiDetailsRequest)
      .map(
        _ => ()
      )
  }

  def addFinancialInstitution(subscriptionId: String, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] = {
    val fiDetailsRequest = buildCreateFiDetailsRequest(subscriptionId, userAnswers)
    connector
      .addOrUpdateFI(fiDetailsRequest)
      .map(
        _ => ()
      )
  }

  def removeFinancialInstitution(details: FIDetail)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] = {
    val removeFIDetail =
      RemoveFIDetail(details.SubscriptionID, details.FIID)
    connector.removeFi(removeFIDetail).map(_.body)
  }

  private def buildCreateFiDetailsRequest(subscriptionId: String, userAnswers: UserAnswers): BaseFIDetail =
    (for {
      fiName  <- userAnswers.get(NameOfFinancialInstitutionPage)
      address <- extractAddress(userAnswers)
    } yield CreateFIDetails(
      FIName = fiName,
      SubscriptionID = subscriptionId,
      TINDetails = extractTinDetails(userAnswers),
      IsFIUser = userAnswers.get(ReportForRegisteredBusinessPage).contains(true),
      IsFATCAReporting = true,
      AddressDetails = address,
      PrimaryContactDetails = extractPrimaryContactDetails(userAnswers),
      SecondaryContactDetails = extractSecondaryContactDetails(userAnswers)
    )).getOrElse(throw new IllegalStateException("Unable to build FIDetail"))

  private def buildUpdateFiDetailsRequest(subscriptionId: String, userAnswers: UserAnswers): BaseFIDetail =
    (for {
      fiid    <- userAnswers.get(ChangeFiDetailsInProgressId)
      fiName  <- userAnswers.get(NameOfFinancialInstitutionPage)
      address <- extractAddress(userAnswers)
    } yield FIDetail(
      FIID = fiid,
      FIName = fiName,
      SubscriptionID = subscriptionId,
      TINDetails = extractTinDetails(userAnswers),
      IsFIUser = userAnswers.get(ReportForRegisteredBusinessPage).contains(true),
      IsFATCAReporting = true,
      AddressDetails = address,
      PrimaryContactDetails = extractPrimaryContactDetails(userAnswers),
      SecondaryContactDetails = extractSecondaryContactDetails(userAnswers)
    )).getOrElse(throw new IllegalStateException("Unable to build FIDetail"))

  private def extractTinDetails(userAnswers: UserAnswers): Seq[TINDetails] = {
    val crn = userAnswers.get(CompanyRegistrationNumberPage) match {
      case Some(crn) => Seq(TINDetails(CRN, crn.value, "GB"))
      case _         => Seq.empty
    }
    val utr = userAnswers.get(WhatIsUniqueTaxpayerReferencePage) match {
      case Some(utr) => Seq(TINDetails(UTR, utr.value, "GB"))
      case _         => Seq.empty
    }
    val trn = userAnswers.get(TrustURNPage) match {
      case Some(trn) => Seq(TINDetails(TRN, trn.value, "GB"))
      case _         => Seq.empty
    }

    val giin = userAnswers.get(WhatIsGIINPage) match {
      case Some(giin) => Seq(TINDetails(GIIN, giin.value, "US"))
      case _          => Seq.empty
    }
    utr ++ crn ++ trn ++ giin
  }

  private def extractPrimaryContactDetails(userAnswers: UserAnswers): Option[ContactDetails] = for {
    contactName  <- userAnswers.get(FirstContactNamePage)
    contactEmail <- userAnswers.get(FirstContactEmailPage)
  } yield ContactDetails(
    ContactName = contactName,
    EmailAddress = contactEmail,
    PhoneNumber = userAnswers.get(FirstContactPhoneNumberPage)
  )

  private def extractSecondaryContactDetails(userAnswers: UserAnswers): Option[ContactDetails] = for {
    contactName  <- userAnswers.get(SecondContactNamePage)
    contactEmail <- userAnswers.get(SecondContactEmailPage)
  } yield ContactDetails(
    ContactName = contactName,
    EmailAddress = contactEmail,
    PhoneNumber = userAnswers.get(SecondContactPhoneNumberPage)
  )

  private def extractAddress(userAnswers: UserAnswers): Option[AddressDetails] =
    userAnswers
      .get(FetchedRegisteredAddressPage)
      .map(_.toAddress)
      .orElse(userAnswers.get(UkAddressPage))
      .orElse(userAnswers.get(NonUkAddressPage))
      .orElse(userAnswers.get(SelectedAddressLookupPage).map(_.toAddress))
      .map(
        address =>
          AddressDetails(
            AddressLine1 = address.addressLine1,
            AddressLine2 = address.addressLine2,
            AddressLine3 = address.addressLine3,
            AddressLine4 = address.addressLine4,
            CountryCode = Some(address.country.code),
            PostalCode = address.postCode
          )
      )

}
