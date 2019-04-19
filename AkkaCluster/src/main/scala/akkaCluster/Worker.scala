package akkaCluster

import akka.actor._
import akkaCluster.Master._
import akkaCluster.Worker._

object Worker {

  case class Work(jobName: String, master: ActorRef)
  case class Task(input: List[String], master: ActorRef)
  case object EmptyWorkLoad

}

class Worker extends Actor with ActorLogging {

  import context._

  var processed = 0

  def receive = {
    case Work(jobName, master) =>
      become(enlisted(jobName, master))
      log.info(s"Enqueuing  work for '${jobName}'.")
      master ! EnQueue(self)
      master ! NextTask
      watch(master)
  }

  def enlisted(jobName: String, master: ActorRef): Receive = {

    case Task(textPart, master) =>
      val countMap = textPart.
        flatMap(_.split("\\W+")).
        foldLeft(Map.empty[String, Int]) (
          (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
        )
      processed = processed + 1
      master ! CompletedTask(countMap)
      master ! NextTask

    case EmptyWorkLoad =>
      log.info(s"Work load ${jobName} is empty. Terminating job.")
      become {
        case Terminated(master) =>
          stop(self)
        case _ =>
          log.error("Terminated")
      }

    case Terminated(master) =>
      stop(self)

  }

}
