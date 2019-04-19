package akkaHttpAndStreams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Merge, Sink, Source}

import scala.concurrent.duration._

/**
  * How to merge two Sources in one flow
  */
object SourcesMergeExample {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()

    implicit val ec = system.dispatcher

    val source1 = Source(0 to 1000).filter(_ % 2 != 0)
    val source2 = Source(1000 to 2000).filter(_ % 2 == 0)
    val flow = Flow[Int].map(identity).throttle(elements = 1, per = 100.millis)

    Source.combine(source2, source1)(Merge(_)).
      via(flow).
      runWith(Sink.foreach(println(_))).
      onComplete(_ => system.terminate)

  }

}
