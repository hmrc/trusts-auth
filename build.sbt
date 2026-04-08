val appName = "trusts-auth"

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / majorVersion := 0

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
