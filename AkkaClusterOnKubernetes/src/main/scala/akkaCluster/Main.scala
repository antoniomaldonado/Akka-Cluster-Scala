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
import java.net._
import java.io._
import scala.collection.mutable.ArrayBuffer
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
    val workerRouter = createRouter("workerRouter", "worker", "/user/worker")
    system.actorOf(Master.props(workerRouter), name = "master")
  }

  if (cluster.selfRoles.contains("wordcount")) {
    val counterRouter = createRouter("masterRouter", "master", "/user/master")
    val wordCounter = system.actorOf(WordCounter.props(counterRouter), name = "wordcount")

    val route =
      path("") {
        get {
          parameter("msg") { (url) =>
            val lines = read(url)
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

  private def createRouter(routerName: String, role: String, routeesPath: String) = {
    system.actorOf(
      ClusterRouterGroup(
        RoundRobinGroup(Nil),
        ClusterRouterGroupSettings(
          totalInstances = 1000,
          routeesPaths = List(routeesPath),
          allowLocalRoutees = true,
          useRole = Some(role)
        )
      ).props(),
      name = routerName)
  }

  def read(urlStr:String):List[String] = {
    val url = new URL(urlStr)
    val in = new BufferedReader(new InputStreamReader(url.openStream))
    val buffer = new ArrayBuffer[String]()
    var inputLine = in.readLine
    while (inputLine != null) {
      if (!inputLine.trim.equals("")) {
        buffer += inputLine.trim
      }
      inputLine = in.readLine
    }
    in.close

    buffer.toList
  }

}

