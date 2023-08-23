import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val bootstrapVersion = "7.21.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "com.typesafe.play"       %% "play-json-joda"             % "2.9.4"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapVersion        % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.scalatest"           %% "scalatest"                % "3.2.16"                 % "test",
    "org.scalatestplus"       %% "mockito-3-4"              % "3.3.0.0-SNAP3"         % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"                 % "test, it",
    "com.vladsch.flexmark"    % "flexmark-all"              % "0.35.10"               % "test, it",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.2",
    "org.mockito"             %  "mockito-all"              % "1.10.19",
    "org.scalacheck"          %% "scalacheck"               % "1.17.0"                % "test"
  )

  val akkaVersion = "2.6.7"
  val akkaHttpVersion = "10.1.12"

  val overrides: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream_2.12"    % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf_2.12"  % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j_2.12"     % akkaVersion,
    "com.typesafe.akka" %% "akka-actor_2.12"     % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core_2.12" % akkaHttpVersion,
    "commons-codec"     %  "commons-codec"       % "1.12"
  )

}
