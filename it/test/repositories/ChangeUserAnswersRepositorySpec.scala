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

package repositories

import config.{CryptoProvider, FrontendAppConfig}
import models.CryptoType.{CryptoT, randomAesKey}
import models.UserAnswers
import org.mockito.Mockito.when
import org.mongodb.scala.model.{Filters, IndexModel}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.Crypted
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class ChangeUserAnswersRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.changeAnswersCacheTtl) thenReturn 1

  when(mockAppConfig.encryptionEnabled) thenReturn true

  implicit val crypto: CryptoT = new CryptoProvider(Configuration("crypto.key" -> randomAesKey)).get()

  override protected val repository = new ChangeUserAnswersRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = userAnswers copy (id = "fatcaid-fiid", lastUpdated = instant)

      val setResult     = repository.set("fatcaid", Some("fiid"), userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", "fatcaid-fiid")).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord mustEqual expectedResult
    }

    "must encrypt the data" in {
      repository.set("fatcaid", Some("fiid"), userAnswers).futureValue

      val raw = mongoComponent.database
        .getCollection("change-user-answers")
        .find(Filters.equal("_id", "fatcaid-fiid"))
        .headOption()
        .futureValue

      raw match {
        case None => fail("db record not found")
        case Some(ua) =>
          val id            = ua.get("_id").head.asString.getValue
          val rawData       = ua.get("data").get.asString.getValue
          val decryptedData = crypto.decrypt(Crypted(rawData)).value

          id mustBe "fatcaid-fiid"
          Json.parse(decryptedData) mustBe userAnswers.data
      }
    }

  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(userAnswers).futureValue

        val result         = repository.get(userAnswers.id).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {

      insert(userAnswers.copy(id = "fatcaid-fiid")).futureValue

      val result = repository.clear("fatcaid", Some("fiid")).futureValue

      result mustEqual true
      repository.get(userAnswers.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("unknown", None).futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(userAnswers).futureValue

        val result = repository.keepAlive(userAnswers.id).futureValue

        val expectedUpdatedAnswers = userAnswers copy (lastUpdated = instant)

        result mustEqual true
        val updatedAnswers = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }
  }

}