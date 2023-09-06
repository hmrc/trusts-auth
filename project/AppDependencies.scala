import sbt.*

object AppDependencies {

  val bootstrapVersion = "7.21.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "com.typesafe.play"       %% "play-json-joda"             % "2.9.4"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatest"           %% "scalatest"                % "3.2.16",
    "org.scalatestplus"       %% "scalacheck-1-17"          % "3.2.16.0",
    "org.scalatestplus"       %% "mockito-4-11"             % "3.2.16.0",
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.8",
    "org.wiremock"            %  "wiremock-standalone"      % "3.0.1",
    "org.mockito"             %  "mockito-all"              % "1.10.19",
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

  val akkaVersion = "2.6.21"
  val akkaHttpVersion = "10.2.10"

  val overrides: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream"        % akkaVersion force(),
    "com.typesafe.akka" %% "akka-protobuf"      % akkaVersion force(),
    "com.typesafe.akka" %% "akka-slf4j"         % akkaVersion force(),
    "com.typesafe.akka" %% "akka-actor"         % akkaVersion force(),
    "com.typesafe.akka" %% "akka-http-core"     % akkaHttpVersion,
    "commons-codec"     %  "commons-codec"      % "1.12"
  )

}
