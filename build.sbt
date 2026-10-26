import uk.gov.hmrc.DefaultBuildSettings.targetJvm

val appName = "trusts-auth"

ThisBuild / scalaVersion := "3.9.0"
ThisBuild / majorVersion := 1
ThisBuild / targetJvm := "jvm-21"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    ),
    libraryDependencies ++= AppDependencies()
  )
  .settings(CodeCoverageSettings())
  .settings(PlayKeys.playDefaultPort := 9794)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
