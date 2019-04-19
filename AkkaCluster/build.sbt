
name := "Akka-Scala"

version := "0.1"

val akkaVersion = "2.5.21"
val akkaManagementVersion = "0.18.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-stream"        % akkaVersion ,
  "com.typesafe.akka"       %% "akka-http"          % "10.1.8",
  "com.typesafe.akka"       %% "akka-testkit"       % akkaVersion     % "test",
  "org.scalatest"           %% "scalatest"          % "3.0.0"         % "test",
  "com.typesafe.akka"       %% "akka-cluster"       % akkaVersion,

  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion
)

resolvers += Classpaths.typesafeReleases

mainClass in assembly := Some("akkaCluster.Main")

assemblyJarName in assembly := "wordCount.jar"