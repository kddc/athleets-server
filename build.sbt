lazy val akkaHttpVersion      = "10.1.7"
lazy val akkaVersion          = "2.5.19"
lazy val reactiveMongoVersion = "0.13.0"
lazy val sttpVersion          = "1.5.7"

organization := "gg.athleets"
scalaVersion := "2.12.7"
name         := "athleets"

libraryDependencies ++= Seq(
  // http client
  "com.softwaremill.sttp"       %% "akka-http-backend"        % sttpVersion,
  "com.softwaremill.sttp"       %% "core"                     % sttpVersion,
  "com.softwaremill.sttp"       %% "play-json"                % sttpVersion,
  // akka
  "com.typesafe.akka"           %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka"           %% "akka-stream"              % akkaVersion,
  "de.heikoseeberger"           %% "akka-http-play-json"      % "1.22.0",
  // test
  "com.typesafe.akka"           %% "akka-http-testkit"        % akkaHttpVersion % Test,
  "com.typesafe.akka"           %% "akka-testkit"             % akkaVersion     % Test,
  "com.typesafe.akka"           %% "akka-stream-testkit"      % akkaVersion     % Test,
  "org.scalatest"               %% "scalatest"                % "3.0.5"         % Test,
  // logging
  "com.typesafe.scala-logging"  %% "scala-logging"            % "3.5.0",
  "com.typesafe.akka"           %% "akka-slf4j"               % akkaVersion,
  "ch.qos.logback"              %  "logback-classic"          % "1.2.3",
  "org.apache.logging.log4j"    %  "log4j-to-slf4j"           % "2.8.2",
  // mongo
  "org.reactivemongo"           %% "reactivemongo"            % reactiveMongoVersion,
  "org.reactivemongo"           %% "reactivemongo-akkastream" % reactiveMongoVersion,
  // other
  "com.github.t3hnar"           %% "scala-bcrypt"             % "3.1",
  "com.pauldijou"               %% "jwt-core"                 % "1.1.0",
  "com.pauldijou"               %% "jwt-play-json"            % "1.1.0"
)

scalacOptions += ""