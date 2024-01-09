package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait InstitutionSelectAddress

object InstitutionSelectAddress extends Enumerable.Implicits {

  case object Address1 extends WithName("address1") with InstitutionSelectAddress
  case object Address2 extends WithName("address2") with InstitutionSelectAddress

  val values: Seq[InstitutionSelectAddress] = Seq(
    Address1, Address2
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"institutionSelectAddress.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[InstitutionSelectAddress] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
