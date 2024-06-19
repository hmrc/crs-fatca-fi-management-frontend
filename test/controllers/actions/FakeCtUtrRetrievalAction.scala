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

package controllers.actions

import models.UniqueTaxpayerReference
import models.requests.IdentifierRequest
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class FakeCtUtrRetrievalActionProvider extends CtUtrRetrievalAction {

  def apply(): ActionFunction[IdentifierRequest, IdentifierRequest] =
    new FakeCtUtrRetrievalAction()

}

class FakeCtUtrRetrievalAction extends ActionFunction[IdentifierRequest, IdentifierRequest] {

  override def invokeBlock[A](request: IdentifierRequest[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
    block(request.copy(autoMatched = true, ctutr = Some(UniqueTaxpayerReference("1234567890"))))

  implicit override protected val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

}
