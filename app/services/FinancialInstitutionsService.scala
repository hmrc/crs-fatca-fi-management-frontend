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
import models.FinancialInstitutions.TINType.{CRN, TURN, UTR}
import models.FinancialInstitutions._
import models.UserAnswers
import models.error.ApiError.{JsValidationError, UnexpectedResponse}
import models.readFIs.response.{ErrorResponse, ViewFIDetails, ViewFIDetailsResponse}
import pages.addFinancialInstitution.IsRegisteredBusiness.{FetchedRegisteredAddressPage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import pages.changeFinancialInstitution.ChangeFiDetailsInProgressId
import pages.{CompanyRegistrationNumberPage, TrustURNPage}
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpErrorFunctions._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsService @Inject() (connector: FinancialInstitutionsConnector) {

  def getListOfFinancialInstitutions(subscriptionId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[FIDetails]] =
    connector
      .viewFis(subscriptionId)
      .flatMap {
        case res if is2xx(res.status) =>
          Json.parse(res.body).validate[ViewFIDetailsResponse] match {
            case JsSuccess(viewFIDetailsResponse, _) => Future.successful(viewFIDetailsResponse.ViewFIDetails.ResponseDetails.FIDetails)
            case JsError(_) =>
              println(Console.BLUE + s"JSON validation error while parsing FIDetails: ${Json.parse(res.body)}" + Console.RESET)
              Future.failed(throw JsValidationError)
          }
        case res =>
          Json.parse(res.body).validate[ErrorResponse] match {
            case JsSuccess(errorResponse, _) =>
              if (res.status == 422 && errorResponse.errorDetail.errorCode.contains("001"))
                Future.successful(Seq.empty) // 001 - No matching records found for the request
              else Future.failed(throw UnexpectedResponse)
            case JsError(_) =>
              println(Console.BLUE + s"JSON validation error while parsing eRRORdETAILS: ${Json.parse(res.body)}" + Console.RESET)
              Future.failed(throw JsValidationError)
          }
      }

  def getFinancialInstitution(subscriptionId: String, fiId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[FIDetails]] =
    connector
      .viewFi(subscriptionId, fiId)
      .map(
        res => extractList(res.body).headOption
      )

  def getInstitutionById(details: Seq[FIDetails], fiid: String): Option[FIDetails] =
    details
      .find(
        detail => detail.FIID == fiid
      )
      .fold[Option[FIDetails]](None)(Some(_))

  def extractList(body: String): Seq[FIDetails] = {
    val json: JsValue                         = Json.parse(body)
    val listsResult: JsResult[Seq[FIDetails]] = (json \ "ViewFIDetails" \ "ResponseDetails" \ "FIDetails").validate[Seq[FIDetails]]

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
      .updateFI(fiDetailsRequest)
      .map(
        _ => ()
      )
  }

  def addFinancialInstitution(subscriptionId: String, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[SubmitFIDetailsResponse] = {
    val fiDetailsRequest = buildCreateFiDetailsRequest(subscriptionId, userAnswers)
    connector
      .addFI(fiDetailsRequest)
      .map(
        res => Json.parse(res.body).as[SubmitFIDetailsResponse]
      )
  }

  def removeFinancialInstitution(details: FIDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] = {
    val removeFIDetail =
      RemoveFIDetail(details.SubscriptionID, details.FIID)
    connector.removeFi(removeFIDetail).map(_.body)
  }

  private def buildCreateFiDetailsRequest(subscriptionId: String, userAnswers: UserAnswers): CreateFIDetails =
    (for {
      fiName  <- userAnswers.get(NameOfFinancialInstitutionPage)
      address <- extractAddress(userAnswers)
    } yield CreateFIDetails(
      FIName = fiName,
      SubscriptionID = subscriptionId,
      TINDetails = extractTinDetails(userAnswers),
      GIIN = userAnswers.get(WhatIsGIINPage).map(_.value),
      IsFIUser = userAnswers.get(ReportForRegisteredBusinessPage).contains(true),
      AddressDetails = address,
      PrimaryContactDetails = extractPrimaryContactDetails(userAnswers),
      SecondaryContactDetails = extractSecondaryContactDetails(userAnswers)
    )).getOrElse(throw new IllegalStateException("Unable to build FIDetail"))

  private def buildUpdateFiDetailsRequest(subscriptionId: String, userAnswers: UserAnswers): FIDetails =
    (for {
      fiid    <- userAnswers.get(ChangeFiDetailsInProgressId)
      fiName  <- userAnswers.get(NameOfFinancialInstitutionPage)
      address <- extractAddress(userAnswers)
    } yield FIDetails(
      FIID = fiid,
      FIName = fiName,
      SubscriptionID = subscriptionId,
      TINDetails = extractTinDetails(userAnswers),
      GIIN = userAnswers.get(WhatIsGIINPage).map(_.value),
      IsFIUser = userAnswers.get(ReportForRegisteredBusinessPage).contains(true),
      AddressDetails = address,
      PrimaryContactDetails = extractPrimaryContactDetails(userAnswers),
      SecondaryContactDetails = extractSecondaryContactDetails(userAnswers)
    )).getOrElse(throw new IllegalStateException("Unable to build FIDetail"))

  private def extractTinDetails(userAnswers: UserAnswers): Option[Seq[TINDetails]] = {
    val details = Seq(
      userAnswers
        .get(WhatIsUniqueTaxpayerReferencePage)
        .map(
          utr => TINDetails(UTR, utr.value, "GB")
        ),
      userAnswers
        .get(CompanyRegistrationNumberPage)
        .map(
          crn => TINDetails(CRN, crn.value, "GB")
        ),
      userAnswers
        .get(TrustURNPage)
        .map(
          trn => TINDetails(TURN, trn.value, "GB")
        )
    ).flatten

    if (details.nonEmpty) Some(details) else None
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
      .get(UkAddressPage)
      .orElse(userAnswers.get(SelectedAddressLookupPage).map(_.toAddress))
      .orElse(userAnswers.get(FetchedRegisteredAddressPage).map(_.toAddress))
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
