package akkaCluster

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor._
import org.scalatest._
import akkaCluster.WordCounter._
import akka.routing.BroadcastPool

trait CreateLocalWorkerRouter extends CreateRouter {
  this: Actor =>
  override def createRouter: ActorRef = {
    context.actorOf(BroadcastPool(1).props(Props[Worker]), "worker-router-test")
  }
}

class JobMaster_ extends Master with CreateLocalWorkerRouter
class WordCounter_ extends WordCounter with CreateMaster {
  override def createMaster(name: String): ActorRef = context.actorOf(Props[JobMaster_], name)
}

class CountWordsSpec extends TestKit(ActorSystem("test")) with WordSpecLike with ImplicitSender {

  val wordCounter = system.actorOf(Props[WordCounter_], "wordCounter")

  "Cluster test" must {
    "count words" in {
      wordCounter ! JobRequest("testCountWords", List("the process of learning an art can be divided conveniently into two parts: one, the mastery of the theory; the other, the mastery of the practice","the mastery"),1)
      expectMsg(JobSuccess("testCountWords", Map("art" -> 1, "conveniently" -> 1, "mastery" -> 3, "process" -> 1, "two" -> 1, "divided" -> 1, "can" -> 1, "theory" -> 1, "an" -> 1, "be" -> 1, "into" -> 1, "practice" -> 1, "learning" -> 1, "other" -> 1, "of" -> 3, "one" -> 1, "the" -> 7, "parts" -> 1)))
      expectNoMsg
    }
  }

}
