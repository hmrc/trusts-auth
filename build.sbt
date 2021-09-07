
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "trusts-auth"

val excludedPackages = Seq(
  "<empty>",
  ".*Reverse.*",
  ".*Routes.*",
  ".*standardError*.*",
  ".*main_template*.*",
  "uk.gov.hmrc.BuildInfo",
  "app.*",
  "prod.*",
  "config.*",
  "testOnlyDoNotUseInAppConf.*",
  "views.html.*",
  "testOnly.*",
  "com.kenshoo.play.metrics*.*",
  ".*LocalDateService.*",
  ".*LocalDateTimeService.*",
  ".*RichJsValue.*",
  ".*Repository.*"
)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    inConfig(Test)(testSettings),
    majorVersion := 0,
    scalaVersion := "2.12.12",
    SilencerSettings(),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    dependencyOverrides ++= AppDependencies.overrides
  )
  .settings(scoverageSettings)
  .settings(PlayKeys.playDefaultPort := 9794)
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)


