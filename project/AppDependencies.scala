import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(

    "uk.gov.hmrc"             %% "bootstrap-backend-play-27"  % "3.0.0",
    "com.typesafe.play"       %% "play-json-joda"             % "2.9.2"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.25.1",
    "org.mockito"             %  "mockito-all"              % "1.10.19",
    "org.scalacheck"          %% "scalacheck"               % "1.14.3"                % "test",
    "wolfendale"              %% "scalacheck-gen-regexp"    % "0.1.2"                 % "test"
  )

  val akkaVersion = "2.6.7"
  val akkaHttpVersion = "10.1.12"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream_2.12"    % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf_2.12"  % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j_2.12"     % akkaVersion,
    "com.typesafe.akka" %% "akka-actor_2.12"     % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core_2.12" % akkaHttpVersion,
    "commons-codec"     %  "commons-codec"       % "1.12"
  )

}
