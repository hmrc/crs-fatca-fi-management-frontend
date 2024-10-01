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

import config.FrontendAppConfig
import models.Country
import play.api.Environment
import play.api.libs.json.Json
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import javax.inject.{Inject, Singleton}

@Singleton
class CountryListFactory @Inject() (environment: Environment, appConfig: FrontendAppConfig) {

  val countryCodesForUkCountries: Set[String] = Set("GB", "UK", "GG", "JE", "IM")

  lazy val countryList: Seq[Country] = getCountryList

  private def getCountryList: Seq[Country] =
    (environment.resourceAsStream(appConfig.countryCodeJson) map Json.parse map {
      _.as[Seq[Country]]
        .sortWith(
          (country, country2) => country.description.toLowerCase < country2.description.toLowerCase
        )
        .distinct
    }).getOrElse(throw new IllegalStateException("Could not retrieve countries list from JSON file."))

  lazy val countryListWithUKCountries: Seq[Country] = countryList
    .filter(
      country => countryCodesForUkCountries.contains(country.code)
    )

  lazy val countryListWithoutUKCountries: Seq[Country] = countryList.filter(
    country => !countryCodesForUkCountries.contains(country.code)
  )

  def countrySelectList(value: Map[String, String], countries: Seq[Country]): Seq[SelectItem] = {
    def containsCountry(country: Country): Boolean =
      value.get("country") match {
        case Some(countryCode) => countryCode == country.code
        case _                 => false
      }

    val countryJsonList = countries.map {
      country =>
        SelectItem(Some(country.code), country.description, containsCountry(country))
    }
    SelectItem(None, "") +: countryJsonList
  }

  def findCountryWithCode(code: String): Option[Country] = countryList.find(_.code.equalsIgnoreCase(code))

}
