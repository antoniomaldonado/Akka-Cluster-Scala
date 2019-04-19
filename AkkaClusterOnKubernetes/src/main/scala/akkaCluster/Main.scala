package akkaCluster

import akka.actor.{ActorSystem, CoordinatedShutdown, Props}
import akka.cluster.Cluster
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.routing.RoundRobinGroup
import akka.stream.ActorMaterializer
import akka.util.Timeout
import WordCounter.JobRequest
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object SimpleClusterApp extends App {

  val baseConfig = ConfigFactory.load()
  val overrideConfig = sys.env.get("CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val config = ConfigFactory.parseString(overrideConfig).withFallback(baseConfig)

  implicit val system = ActorSystem("AkkaCluster", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val cluster = Cluster(system)

  if (cluster.selfRoles.contains("worker")) {
    system.actorOf(Props[Worker], name = "worker")
  }

  if (cluster.selfRoles.contains("master")) {

    val workerRouter = system.actorOf(
      ClusterRouterGroup(
        RoundRobinGroup(Nil),
        ClusterRouterGroupSettings(
          totalInstances = 1000,
          routeesPaths = List("/user/worker"),
          allowLocalRoutees = true,
          useRole = Some("worker")
        )
      ).props(),
      name = "workerRouter")

    system.actorOf(Master.props(workerRouter), name = "master")

  }

    if (cluster.selfRoles.contains("wordcounter")) {
    val counterRouter = system.actorOf(
      ClusterRouterGroup(
        RoundRobinGroup(Nil),
        ClusterRouterGroupSettings(
          totalInstances = 1000,
          routeesPaths = List("/user/master"),
          allowLocalRoutees = true,
          useRole = Some("master")
        )
      ).props(),
      name = "counterRouter")

    val wordCounter = system.actorOf(WordCounter.props(counterRouter), name = "wordcounter")

    val route =
      path("") {
        get {
          parameter("msg") { (message) =>

            // TODO read the msg from a post
            val lines = List("haha kkeek fjfjf", "sdfsd dfdf  haha")
            val linesPerJob = 20

            implicit val timeout: Timeout = 1.second
            wordCounter ? JobRequest("Job", lines, linesPerJob)
            complete("ok")

          }
        }
      } ~ path("health") {
        get {
          complete("OK")
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

    CoordinatedShutdown(system).addJvmShutdownHook({
      bindingFuture
        .flatMap(_.unbind())
    })

  }

}

