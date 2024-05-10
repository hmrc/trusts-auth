
val appName = "trusts-auth"

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedFiles := "<empty>;.*components.*;.*Mode.*;.*Routes.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 91,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    ),
    libraryDependencies ++= AppDependencies()
  )
  .settings(scoverageSettings)
  .settings(PlayKeys.playDefaultPort := 9794)

addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
