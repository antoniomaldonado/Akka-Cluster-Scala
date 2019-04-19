package akkaHttpAndStreams

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Client server example of Tcp binding and streams
  */
object TCPStreamExample {

  // The actor system with the actors that compose our stream
  implicit val system = ActorSystem()
  // The materializer that transforms the stream flow into actors
  implicit val mat = ActorMaterializer()

  def main(args: Array[String]): Unit = {

    // Our socket address
    val addr = new InetSocketAddress("localhost", 8888)

    // A TCP stream will bind to a Server at some Future time
    val server: Future[ServerBinding] =
      Tcp().bind(addr.getHostName, addr.getPort).to(Sink.foreach { conn =>
        conn.flow.join(Flow[ByteString]).run()
      }).run()

    println("TCP Stream is bound to port: " + Await.result(server, 1.second))

    // Outgoing connection from the client to the server
    val client = Tcp().outgoingConnection(addr)

    // Send some text from the source with the client connection to the server
    val source = Source.fromIterator(() => Iterator from 0).throttle(elements = 1, per = 100.millis)

    // back and forth Bidirectional flow between the println operation, the client, the server and the source
    source.
      map(x => ByteString(s"Iteration $x")).
      via(client).
      map(_.decodeString("UTF-8")).
      runForeach(println)

  }

}
