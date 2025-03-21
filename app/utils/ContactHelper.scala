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

package utils

import models.UserAnswers
import pages.addFinancialInstitution.{FirstContactNamePage, NameOfFinancialInstitutionPage, SecondContactNamePage}
import play.api.i18n.Messages
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter

trait ContactHelper {

  def getFirstContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(FirstContactNamePage)
      .fold(messages("default.firstContact.name"))(
        contactName => contactName
      )

  def getSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(SecondContactNamePage)
      .fold(messages("default.secondContact.name"))(
        contactName => contactName
      )

  def getFinancialInstitutionName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(NameOfFinancialInstitutionPage)
      .fold(messages("default.FI.name"))(
        fiName => fiName
      )

  // Function to format date
  def formatDate(date: LocalDate): String = {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(dateFormatter)
  }

  // Function to format time
  def formatTime(time: LocalTime): String =
    if (time == LocalTime.MIDNIGHT) {
      s"midnight"
    } else if (time == LocalTime.NOON) {
      s"midday"
    } else {
      val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
      val formattedTime = time.format(timeFormatter).toLowerCase
      formattedTime
    }

}
