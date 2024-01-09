package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class InstitutionSelectAddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "InstitutionSelectAddress" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(InstitutionSelectAddress.values.toSeq)

      forAll(gen) {
        institutionSelectAddress =>

          JsString(institutionSelectAddress.toString).validate[InstitutionSelectAddress].asOpt.value mustEqual institutionSelectAddress
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!InstitutionSelectAddress.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[InstitutionSelectAddress] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(InstitutionSelectAddress.values.toSeq)

      forAll(gen) {
        institutionSelectAddress =>

          Json.toJson(institutionSelectAddress) mustEqual JsString(institutionSelectAddress.toString)
      }
    }
  }
}
