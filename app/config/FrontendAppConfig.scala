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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$appName&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String                          = configuration.get[String]("urls.login")
  val loginContinueUrl: String                  = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String                        = configuration.get[String]("urls.signOut")
  lazy val lostUTRUrl: String                   = configuration.get[String]("urls.lostUTR")
  lazy val searchCrn: String                    = configuration.get[String]("urls.searchCrn")
  lazy val addressLookUpUrl: String             = configuration.get[Service]("microservice.services.address-lookup").baseUrl
  lazy val registrationUrl: String              = configuration.get[Service]("microservice.services.crs-fatca-registration").baseUrl
  lazy val fIManagementUrl: String              = configuration.get[Service]("microservice.services.crs-fatca-fi-management").baseUrl
  lazy val registerUrl: String                  = configuration.get[String]("urls.register")
  lazy val changeOrganisationDetailsUrl: String = s"$registerUrl/change-contact/organisation/details"
  lazy val changeIndividualDetailsUrl: String   = s"$registerUrl/change-contact/individual/details"
  lazy val emailEnquiries: String               = configuration.get[String]("urls.emailEnquiries")

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/$appName"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int              = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  val changeAnswersCacheTtl: Int = configuration.get[Int]("mongodb.changeAnswersTimeToLiveInSeconds")

  val enrolmentKey: String   = configuration.get[String]("keys.enrolmentKey.crsFatca")
  val ctEnrolmentKey: String = configuration.get[String]("keys.enrolmentKey.ct")

  lazy val countryCodeJson: String = configuration.get[String]("json.countries")

  lazy val encryptionEnabled: Boolean = configuration.get[Boolean]("mongodb.encryptionEnabled")
}
