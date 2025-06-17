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

package utils

import org.scalatest.TestData
import play.api.i18n.Lang.logger

import java.nio.file.{Files, Paths, StandardOpenOption}
import java.nio.charset.StandardCharsets

object SnapshotUtils {

  private val snapshotDir = "test/snapshots"

  val shouldUpdateSnapshots: Boolean = sys.env.get("UPDATE_SNAPSHOTS").contains("true")

  def assertMatchesSnapshot(testName: String, content: String): Unit = {
//    val testNameFromSpec = td.name.replaceAll("\\s+", "-")

    val snapshotPath = Paths.get(snapshotDir, s"$testName.snapshot")

    if (Files.exists(snapshotPath)) {
      val existingSnapshot = new String(Files.readAllBytes(snapshotPath), StandardCharsets.UTF_8)
      assert(
        content == existingSnapshot,
        s"Snapshot mismatch for $testName.\nExpected:\n$existingSnapshot\nActual:\n$content"
      )
    } else {
      logger.info(s"Creating new snapshot for $testName")
      Files.createDirectories(snapshotPath.getParent)
      Files.write(snapshotPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    }
  }

}
