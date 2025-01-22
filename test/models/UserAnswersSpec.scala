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

package models

import base.SpecBase
import org.scalatest.matchers.must.Matchers
import pages.addFinancialInstitution.IsRegisteredBusiness.IsThisYourBusinessNamePage
import pages.addFinancialInstitution.NameOfFinancialInstitutionPage

class UserAnswersSpec extends SpecBase with Matchers {

  "UserAnswers set" - {
    "should cleanup by default" in {
      val usersAnswers = emptyUserAnswers
        .withPage(IsThisYourBusinessNamePage, true)
        .withPage(NameOfFinancialInstitutionPage, "test")
      val result = usersAnswers.set(IsThisYourBusinessNamePage, true).get
      result.data.value must not contain key(NameOfFinancialInstitutionPage.toString)
    }

    "should not cleanup when cleanup flag is false" in {
      val usersAnswers = emptyUserAnswers
        .withPage(IsThisYourBusinessNamePage, false)
        .withPage(NameOfFinancialInstitutionPage, "test")
      val result = usersAnswers.set(IsThisYourBusinessNamePage, false, cleanup = false).get
      result.data.value must contain key NameOfFinancialInstitutionPage.toString
    }
  }

}
