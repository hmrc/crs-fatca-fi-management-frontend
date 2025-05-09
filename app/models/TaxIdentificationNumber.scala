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

package models

import play.api.libs.json._

sealed trait TaxIdentificationNumber {
  def value: String
}

final case class UniqueTaxpayerReference(override val value: String) extends TaxIdentificationNumber

final case class GIINumber(override val value: String) extends TaxIdentificationNumber

final case class CompanyRegistrationNumber(override val value: String) extends TaxIdentificationNumber

final case class TrustUniqueReferenceNumber(override val value: String) extends TaxIdentificationNumber

object UniqueTaxpayerReference {
  implicit val format: OFormat[UniqueTaxpayerReference] = Json.format[UniqueTaxpayerReference]
}

object GIINumber {
  implicit val format: OFormat[GIINumber] = Json.format[GIINumber]
}

object CompanyRegistrationNumber {
  implicit val format: OFormat[CompanyRegistrationNumber] = Json.format[CompanyRegistrationNumber]
}

object TrustUniqueReferenceNumber {
  implicit val format: OFormat[TrustUniqueReferenceNumber] = Json.format[TrustUniqueReferenceNumber]
}
