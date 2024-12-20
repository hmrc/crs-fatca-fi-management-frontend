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

package models.subscription.response

import models.subscription.request.{ContactInformation, OrganisationDetails}
import play.api.libs.json.{Json, OFormat}

case class CrfaSubscriptionDetails(crfaSubscriptionDetails: UserSubscription)

object CrfaSubscriptionDetails {
  implicit lazy val format: OFormat[CrfaSubscriptionDetails] = Json.format[CrfaSubscriptionDetails]
}

case class UserSubscription(
  crfaReference: String,
  tradingName: Option[String],
  gbUser: Boolean,
  primaryContact: ContactInformation,
  secondaryContact: Option[ContactInformation]
) {

  val isBusiness: Boolean =
    primaryContact.contactInformation match {
      case OrganisationDetails(_) => true
      case _                      => false
    }

  val businessName: Option[String] =
    primaryContact.contactInformation match {
      case OrganisationDetails(name) => Some(tradingName.getOrElse(name))
      case _                         => None
    }

}

object UserSubscription {
  implicit val format: OFormat[UserSubscription] = Json.format[UserSubscription]
}
