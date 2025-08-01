import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.16.0"
  private val hmrcMongoVersion = "2.6.0"

  val compile = Seq[ModuleID](
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "12.7.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.3.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "com.beachape"      %% "enumeratum-play"                       % "1.8.1",
    "uk.gov.hmrc"      %% "crypto-json-play-30"    % "8.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"    %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"    %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"          %% "mockito-scala"           % "1.17.37",
    "org.scalacheck"       %% "scalacheck"              % "1.18.0",
    "org.jsoup"             % "jsoup"                   % "1.18.1",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"   % "1.1.0"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
