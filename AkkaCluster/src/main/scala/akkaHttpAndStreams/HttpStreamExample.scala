package akkaHttpAndStreams

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{getFromBrowseableDirectories, pathPrefix}
import akka.stream.ActorMaterializer

/**
  * Example were the url http://localhost:8888/local-dir will show the contents of our local-dir
  */
object HttpStreamExample {

  // The actor system with the actors that will run our stream
  implicit val system = ActorSystem()
  // The materializer that transforms the stream flow into actors
  implicit val mat = ActorMaterializer()

  def main(args: Array[String]): Unit = {

    // Route flow will turn requests to the path local-dir into responses
    val route =
      pathPrefix("local-dir") {
        // response with content in local directory /Users/http
        getFromBrowseableDirectories("/Users/http")
      }

    // Bidirectional Http flow to handle incoming connections.
    Http().bind("localhost", 8888). // server bind to a port
      runForeach(conn => conn.flow.join(route).run()) // connection to our route flow

  }

}
