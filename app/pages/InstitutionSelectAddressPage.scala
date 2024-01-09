package pages

import models.InstitutionSelectAddress
import play.api.libs.json.JsPath

case object InstitutionSelectAddressPage extends QuestionPage[InstitutionSelectAddress] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "institutionSelectAddress"
}
