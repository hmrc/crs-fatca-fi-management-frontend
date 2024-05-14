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
import models.subscription.request.{ContactInformation, IndividualDetails, OrganisationDetails}
import models.subscription.response.UserSubscription
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UserSubscriptionSpec extends SpecBase with ScalaCheckPropertyChecks {

  val mockUserSubscription: UserSubscription = mock[UserSubscription]

  val individualSubscription   = UserSubscription("", None, true, ContactInformation(IndividualDetails("firstName", "lastName"), "", None), None)
  val organisationSubscription = UserSubscription("", Some("trading name"), true, ContactInformation(OrganisationDetails("business"), "", None), None)

  "UserSubscription" - {
    "isBusiness must return true if subscription response contains organisation details" in {
      organisationSubscription.isBusiness mustBe true
    }

    "isBusiness must return false if subscription response contains individual details" in {
      individualSubscription.isBusiness mustBe false
    }

    "businessName must return a trading name if subscription contains a trading name" in {
      organisationSubscription.businessName mustBe Some("trading name")
    }

    "businessName must return a trading name if subscription doesn't contain a trading name but does include organisation details" in {
      organisationSubscription.copy(tradingName = None).businessName mustBe Some("business")
    }

    "businessName must return a None if subscription is an individual" in {
      individualSubscription.businessName mustBe None
    }
  }

}
