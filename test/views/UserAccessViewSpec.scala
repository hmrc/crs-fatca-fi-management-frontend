/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import base.SpecBase
import forms.UserAccessFormProvider
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.Helpers.stubMessages
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import views.html.UserAccessView

class UserAccessViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting {

  val view1: UserAccessView = app.injector.instanceOf[UserAccessView]

  val form = new UserAccessFormProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = stubMessages()

  "UserAccessView" - {
    "individual or sole trader" in {
      val key = "individual"
      val renderedHtml: HtmlFormat.Appendable =
        view1(form(key), isBusiness = false, fiIsUser = false, testFiid, fiName)

      val contentString: String = renderedHtml.toString

      contentString must include(messages("userAccess.heading.individual", fiName))
      contentString must include(messages("userAccess.title.individual", fiName))
    }

    "organisation where FI = USER" in {
      val key = "registeredUser"
      val renderedHtml: HtmlFormat.Appendable =
        view1(form(key), isBusiness = true, fiIsUser = true, testFiid, fiName, Some(testBusinessName))

      val contentString: String = renderedHtml.toString

      contentString must include(messages("userAccess.heading.registeredUser", fiName))
      contentString must include(messages("userAccess.title.registeredUser", fiName))
    }

    "organisation where FI = NOT USER" in {
      val key = "organisation"
      val renderedHtml: HtmlFormat.Appendable =
        view1(form(key), isBusiness = true, fiIsUser = false, testFiid, fiName, Some(testBusinessName))

      val contentString: String = renderedHtml.toString

      contentString must include(messages("userAccess.heading.organisation", testBusinessName, fiName))
      contentString must include(messages("userAccess.title.organisation", fiName))
    }
  }

}
