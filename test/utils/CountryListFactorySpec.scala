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

package utils

import base.SpecBase
import config.FrontendAppConfig
import models.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Inspectors.forAll
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import java.io.ByteArrayInputStream

class CountryListFactorySpec extends SpecBase with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val ukCountryCodes = Set("GB", "UK", "GG", "JE", "IM")

  private val conf = mock[FrontendAppConfig]
  private val env  = mock[Environment]

  override def beforeEach(): Unit = {
    reset(conf, env)
  }

  "Country List Factory" - {

    "countryList" - {
      "must return distinct countries" in {
        val countries = Json.arr(
          Json.obj("state" -> "valid", "code" -> "AB", "description" -> "Country_1"),
          Json.obj("state" -> "valid", "code" -> "AB", "description" -> "Country_1"),
          Json.obj("state" -> "valid", "code" -> "BC", "description" -> "Country_2"),
          Json.obj("state" -> "valid", "code" -> "CD", "description" -> "Country_3")
        )

        when(conf.countryCodeJson).thenReturn("some-countries-list.json")
        val byteArrayInputStream = new ByteArrayInputStream(countries.toString.getBytes)
        when(env.resourceAsStream(any())).thenReturn(Some(byteArrayInputStream))

        val countryListFactory = new CountryListFactory(env, conf)

        countryListFactory.countryList mustBe
          Seq(
            Country("valid", "AB", "Country_1"),
            Country("valid", "BC", "Country_2"),
            Country("valid", "CD", "Country_3")
          )
      }

      "must throw IllegalStateException when country list cannot be loaded from environment" in {
        when(conf.countryCodeJson).thenReturn("some-countries-list.json")
        when(env.resourceAsStream(any())).thenReturn(None)

        val countryListFactory = new CountryListFactory(env, conf)

        an[IllegalStateException] must be thrownBy countryListFactory.countryList
      }
    }
  }

  "countryListWithUKCountries" - {
    "must return countries in the UK" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))

      forAll(factory.countryListWithUKCountries) {
        country =>
          ukCountryCodes must contain(country.code)
      }
    }
  }

  "countryListWithoutUKCountries" - {
    "must return countries not in the UK" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))

      forAll(factory.countryListWithoutUKCountries) {
        country =>
          ukCountryCodes must not contain country.code
      }
    }
  }

  "countrySelectList" - {
    "must return list with no country selected when none is" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))
      val countries = Seq(
        Country("valid", "AB", "Country_1"),
        Country("valid", "BC", "Country_2"),
        Country("valid", "CD", "Country_3")
      )

      factory.countrySelectList(Map.empty, countries) must contain theSameElementsAs Seq(
        SelectItem(value = None, text = "", selected = false),
        SelectItem(value = Some("AB"), text = "Country_1", selected = false),
        SelectItem(value = Some("BC"), text = "Country_2", selected = false),
        SelectItem(value = Some("CD"), text = "Country_3", selected = false)
      )
    }

    "must return list containing selected country" in {
      val factory = new CountryListFactory(app.environment, new FrontendAppConfig(app.configuration))
      val countries = Seq(
        Country("valid", "AB", "Country_1"),
        Country("valid", "BC", "Country_2"),
        Country("valid", "CD", "Country_3")
      )

      val selectedCountry = Map("country" -> "BC")

      factory.countrySelectList(selectedCountry, countries) must contain theSameElementsAs Seq(
        SelectItem(value = None, text = "", selected = false),
        SelectItem(value = Some("AB"), text = "Country_1", selected = false),
        SelectItem(value = Some("BC"), text = "Country_2", selected = true),
        SelectItem(value = Some("CD"), text = "Country_3", selected = false)
      )
    }
  }

}
