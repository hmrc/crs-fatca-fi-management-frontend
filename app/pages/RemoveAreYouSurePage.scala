package pages

import play.api.libs.json.JsPath

case object RemoveAreYouSurePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "removeAreYouSure"
}
