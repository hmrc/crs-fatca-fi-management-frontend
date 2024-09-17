/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import models.{RichJsObject, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.addFinancialInstitution.IsRegisteredBusiness.{IsTheAddressCorrectPage, IsThisYourBusinessNamePage, ReportForRegisteredBusinessPage}
import pages.addFinancialInstitution._
import play.api.libs.json.{JsObject, JsPath, JsValue, Json}

trait UserAnswersGenerator extends UserAnswersEntryGenerators with TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(IsTheAddressCorrectPage.type, JsValue)] ::
      arbitrary[(IsThisYourBusinessNamePage.type, JsValue)] ::
      arbitrary[(ReportForRegisteredBusinessPage.type, JsValue)] ::
      arbitrary[(FirstContactEmailPage.type, JsValue)] ::
      arbitrary[(FirstContactHavePhonePage.type, JsValue)] ::
      arbitrary[(FirstContactNamePage.type, JsValue)] ::
      arbitrary[(FirstContactPhoneNumberPage.type, JsValue)] ::
      arbitrary[(HaveGIINPage.type, JsValue)] ::
      arbitrary[(HaveUniqueTaxpayerReferencePage.type, JsValue)] ::
      arbitrary[(IsThisAddressPage.type, JsValue)] ::
      arbitrary[(NameOfFinancialInstitutionPage.type, JsValue)] ::
      arbitrary[(NonUkAddressPage.type, JsValue)] ::
      arbitrary[(PostcodePage.type, JsValue)] ::
      arbitrary[(SecondContactCanWePhonePage.type, JsValue)] ::
      arbitrary[(SecondContactEmailPage.type, JsValue)] ::
      arbitrary[(SecondContactExistsPage.type, JsValue)] ::
      arbitrary[(SecondContactNamePage.type, JsValue)] ::
      arbitrary[(SecondContactPhoneNumberPage.type, JsValue)] ::
      arbitrary[(SelectAddressPage.type, JsValue)] ::
      arbitrary[(UkAddressPage.type, JsValue)] ::
      arbitrary[(WhatIsGIINPage.type, JsValue)] ::
      arbitrary[(WhatIsUniqueTaxpayerReferencePage.type, JsValue)] ::
      arbitrary[(WhereIsFIBasedPage.type, JsValue)] ::
      Nil

  private def genJsObj(gens: Gen[(QuestionPage[_], JsValue)]*): Gen[JsObject] =
    Gen.sequence[Seq[(QuestionPage[_], JsValue)], (QuestionPage[_], JsValue)](gens).map {
      seq =>
        seq.foldLeft(Json.obj()) {
          case (obj, (page, value)) =>
            obj.setObject(page.path, value).get
        }
    }

  private def setFields(obj: JsObject, fields: (JsPath, JsValue)*): JsObject =
    fields.foldLeft(obj) {
      case (acc, (path, value)) => acc.setObject(path, value).get
    }

  private lazy val ukAddress =
    Arbitrary {
      for {
        postCode          <- genJsObj(arbitrary[(PostcodePage.type, JsValue)])
        isThisYourAddress <- arbitrary[Boolean]
        data <-
          if (isThisYourAddress) {
            genJsObj(arbitrary[(SelectedAddressLookupPage.type, JsValue)])
          } else {
            genJsObj(arbitrary[(UkAddressPage.type, JsValue)])
          }
      } yield postCode ++ data
    }

  private lazy val address =
    Arbitrary {
      for {
        whereDoYouLive <- arbitrary[Boolean]
        data <-
          if (whereDoYouLive) {
            ukAddress.arbitrary
          } else {
            genJsObj(arbitrary[(NonUkAddressPage.type, JsValue)])
          }
        obj = setFields(
          Json.obj(),
          WhereIsFIBasedPage.path -> Json.toJson(whereDoYouLive)
        ) ++ data
      } yield obj
    }

  private def phoneNumberArbitrary[T <: QuestionPage[Boolean], U <: QuestionPage[String]](
    havePhonePage: T,
    phonePage: U
  )(implicit arb: Arbitrary[(phonePage.type, JsValue)]) = Arbitrary {
    for {
      havePhone <- arbitrary[Boolean]
      data <-
        if (havePhone) {
          genJsObj(arbitrary[(phonePage.type, JsValue)])
        } else {
          Gen.const(Json.obj())
        }
      obj = setFields(
        Json.obj(),
        havePhonePage.path -> Json.toJson(havePhone)
      ) ++ data
    } yield obj
  }

  private def pageArbitrary[T <: QuestionPage[_]](page: T)(implicit arb: Arbitrary[(page.type, JsValue)]) = Arbitrary {
    for {
      email <- genJsObj(arbitrary[(page.type, JsValue)])
    } yield email
  }

  private lazy val firstContactDetails = Arbitrary {
    for {
      name  <- genJsObj(arbitrary[(FirstContactNamePage.type, JsValue)])
      email <- genJsObj(arbitrary[(FirstContactEmailPage.type, JsValue)])
      phone <- phoneNumberArbitrary(FirstContactHavePhonePage, FirstContactPhoneNumberPage).arbitrary
    } yield name ++ email ++ phone
  }

  private lazy val secondContactDetails = Arbitrary {
    for {
      name  <- genJsObj(arbitrary[(SecondContactNamePage.type, JsValue)])
      email <- genJsObj(arbitrary[(SecondContactEmailPage.type, JsValue)])
      phone <- phoneNumberArbitrary(SecondContactCanWePhonePage, SecondContactPhoneNumberPage).arbitrary
    } yield name ++ email ++ phone
  }

  private lazy val firstAndSecondContactDetails = Arbitrary {
    for {
      haveSecondContact <- arbitrary[Boolean]
      firstContact      <- firstContactDetails.arbitrary
      secondContact <-
        if (haveSecondContact) {
          secondContactDetails.arbitrary
        } else {
          Gen.const(Json.obj())
        }
      obj = setFields(
        Json.obj(),
        SecondContactExistsPage.path -> Json.toJson(haveSecondContact)
      ) ++ firstContact ++ secondContact
    } yield obj
  }

  private lazy val nameUTRandGIINDetails = Arbitrary {
    for {
      haveUTR  <- arbitrary[Boolean]
      haveGIIN <- arbitrary[Boolean]
      fiName <-
        pageArbitrary(NameOfFinancialInstitutionPage).arbitrary
      utr <-
        if (haveUTR) {
          pageArbitrary(WhatIsUniqueTaxpayerReferencePage).arbitrary
        } else {
          Gen.const(Json.obj())
        }
      giin <-
        if (haveGIIN) {
          pageArbitrary(WhatIsGIINPage).arbitrary
        } else {
          Gen.const(Json.obj())
        }
      obj = setFields(
        Json.obj(),
        HaveUniqueTaxpayerReferencePage.path -> Json.toJson(haveUTR),
        HaveGIINPage.path                    -> Json.toJson(haveGIIN)
      ) ++ fiName ++ utr ++ giin
    } yield obj
  }

  private lazy val registeredBusinessDetails = Arbitrary {
    for {
      reportForRegisteredBusiness <- arbitrary[Boolean]
      isThisBusinessName          <- arbitrary[Boolean]
      haveGIIN                    <- arbitrary[Boolean]
      isAddressCorrect            <- arbitrary[Boolean]
      address                     <- address.arbitrary
      fiName <-
        pageArbitrary(NameOfFinancialInstitutionPage).arbitrary
      report <-
        if (reportForRegisteredBusiness) {
          pageArbitrary(IsThisYourBusinessNamePage).arbitrary
        } else {
          Gen.const(Json.obj())
        }
      businessName <-
        if (isThisBusinessName) {
          Gen.const(Json.obj())
        } else {
          pageArbitrary(NameOfFinancialInstitutionPage).arbitrary
        }
      whatIsGIIN <-
        if (haveGIIN) {
          pageArbitrary(WhatIsGIINPage).arbitrary
        } else {
          Gen.const(Json.obj())
        }
      whereIsFIBased <-
        if (isAddressCorrect) {
          Gen.const(Json.obj())
        } else {
          pageArbitrary(WhereIsFIBasedPage).arbitrary
        }
      obj = setFields(
        Json.obj(),
        ReportForRegisteredBusinessPage.path              -> Json.toJson(reportForRegisteredBusiness),
        IsThisYourBusinessNamePage.path                   -> Json.toJson(isThisBusinessName),
        IsRegisteredBusiness.IsTheAddressCorrectPage.path -> Json.toJson(isAddressCorrect),
        HaveGIINPage.path                                 -> Json.toJson(haveGIIN)
      ) ++ fiName ++ report ++ businessName ++ whatIsGIIN ++ whereIsFIBased ++ address
    } yield obj
  }

  lazy val fiNotRegistered: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      address        <- address.arbitrary
      contactDetails <- firstAndSecondContactDetails.arbitrary
      nameUTRandGIIN <- nameUTRandGIINDetails.arbitrary
      obj =
        setFields(
          Json.obj()
        ) ++ address ++ contactDetails ++ nameUTRandGIIN
    } yield UserAnswers(
      id = id,
      data = obj
    )
  }

  lazy val fiRegistered: Arbitrary[UserAnswers] = Arbitrary {
    for {
      id             <- nonEmptyString
      isThisBusiness <- registeredBusinessDetails.arbitrary
      obj =
        setFields(
          Json.obj()
        ) ++ isThisBusiness
    } yield UserAnswers(
      id = id,
      data = obj
    )
  }

  private def missingAnswersArb(arb: Arbitrary[UserAnswers], possibleAnswers: Seq[QuestionPage[_]]): Arbitrary[UserAnswers] = Arbitrary {
    for {
      answers <- arb.arbitrary
      validPossibleAnswers = possibleAnswers.filter(
        a => answers.data.keys.toSeq.contains(a.toString)
      )
      n                <- Gen.oneOf(1, validPossibleAnswers.length)
      missingQuestions <- Gen.pick(n, validPossibleAnswers)
      updatedAnswers = missingQuestions.foldLeft(answers) {
        case (acc, page) => acc.remove(page).success.value
      }
    } yield updatedAnswers
  }

  lazy val fiNotRegisteredMissingAnswers: Arbitrary[UserAnswers] =
    missingAnswersArb(
      fiNotRegistered,
      Seq(
        NameOfFinancialInstitutionPage,
        FirstContactEmailPage,
        FirstContactHavePhonePage,
        FirstContactNamePage,
        FirstContactPhoneNumberPage,
        HaveGIINPage,
        HaveUniqueTaxpayerReferencePage,
        WhatIsGIINPage,
        WhatIsUniqueTaxpayerReferencePage,
        WhereIsFIBasedPage,
        SecondContactEmailPage,
        SecondContactExistsPage,
        SecondContactNamePage,
        SecondContactPhoneNumberPage
      )
    )

  lazy val fiRegisteredMissingAnswers: Arbitrary[UserAnswers] =
    missingAnswersArb(
      fiRegistered,
      Seq(
        HaveGIINPage,
        WhatIsGIINPage,
        ReportForRegisteredBusinessPage,
        IsThisYourBusinessNamePage,
        IsTheAddressCorrectPage
      )
    )

}
