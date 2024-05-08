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

import base.SpecBase
import connectors.SubscriptionConnector
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails}
import models.subscription.response.UserSubscription
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind

class SubscriptionServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  val individualSubscription   = UserSubscription("", None, true, ContactInformation(IndividualDetails("firstName", "lastName"), "", None), None)
  val organisationSubscription = UserSubscription("", Some("trading name"), true, ContactInformation(OrganisationDetails("business"), "", None), None)

  private val app = applicationBuilder()
    .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))

  lazy val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]
  lazy val userSubscription = service.getSubscription("fatcaId")

  "SubscriptionService" - {
    "isOrganisation must return true if subscription response contains organisation details" in {
      service.(organisationSubscription) mustBe true
    }

    "isOrganisation must return false if subscription response contains individual details" in {
      service.isOrganisation(individualSubscription) mustBe false
    }

    "getBusinessName must return a trading name if subscription contains a trading name" in {
      service.getBusinessName(organisationSubscription) mustBe organisationSubscription.tradingName
    }

    "getBusinessName must return a trading name if subscription doesn't contain a trading name but does include organisation details" in {
      service.getBusinessName(organisationSubscription.copy(tradingName = None)) mustBe Some("business")
    }

    "getBusinessName must return a None if subscription is an individual" in {
      service.getBusinessName(individualSubscription) mustBe None
    }
  }

}
