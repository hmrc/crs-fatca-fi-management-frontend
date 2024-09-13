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
import models.FinancialInstitutions.TINType.UTR
import models.FinancialInstitutions._
import models.UserAnswers
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import play.api.libs.json.{JsResult, JsValue, Json}
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

  def getInstitutionById(details: Seq[FIDetail], fiid: String): Option[FIDetail] =
    details
      .find(
        detail => detail.FIID == fiid
      )
      .fold[Option[FIDetail]](None)(Some(_))

  private def extractList(body: String) = {
    val json: JsValue                        = Json.parse(body)
    val listsResult: JsResult[Seq[FIDetail]] = (json \ "ViewFIDetails" \ "ResponseDetails" \ "FIDetails").validate[Seq[FIDetail]]
    listsResult.get
  }

  def addFinancialInstitution(subscriptionId: String, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] =
    connector
      .addFi(buildFiDetailsRequest(subscriptionId, userAnswers))
      .map(
        _ => ()
      )

  private def buildFiDetailsRequest(subscriptionId: String, userAnswers: UserAnswers): CreateFIDetails =
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

  private def extractTinDetails(userAnswers: UserAnswers): Seq[TINDetails] = {
    val utr = userAnswers.get(WhatIsUniqueTaxpayerReferencePage) match {
      case Some(utr) => Seq(TINDetails(UTR, utr.value, "GB"))
      case _         => Seq.empty
    }

    val giin = userAnswers.get(WhatIsGIINPage) match {
      case Some(giin) => Seq(TINDetails(UTR, giin, "US"))
      case _          => Seq.empty
    }

    utr ++ giin
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
