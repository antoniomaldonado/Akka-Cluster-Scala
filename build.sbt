
name := "Akka-Scala"

version := "0.1"

val akkaVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-stream"        % akkaVersion ,
  "com.typesafe.akka"       %% "akka-http"          % "10.1.8",
  "com.typesafe.akka"       %% "akka-testkit"       % akkaVersion     % "test",
  "org.scalatest"           %% "scalatest"          % "3.0.0"         % "test",
  "com.typesafe.akka"       %% "akka-cluster"       % akkaVersion
)

resolvers += Classpaths.typesafeReleases

mainClass in assembly := Some("akkaCluster.Main")

assemblyJarName in assembly := "wordCount.jar"