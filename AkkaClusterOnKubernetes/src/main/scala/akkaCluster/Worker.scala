package akkaCluster

import java.net.InetAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import Master.{CompletedTask, EnQueue, NextTask}
import Worker.{EmptyWorkLoad, Task, Work}

object Worker {

  case class Work(jobName: String, master: ActorRef)
  case class Task(input: List[String], master: ActorRef)
  case object EmptyWorkLoad

}

class Worker extends Actor with ActorLogging {

  val hostname = InetAddress.getLocalHost.getHostName

  import context._

  var processed = 0

  def receive = {
    case Work(jobName, master) =>
      become(enlisted(jobName, master))
      log.info(s"Enqueuing  work for '${jobName}'.")
      master ! EnQueue(self)
      master ! NextTask
      watch(master)
    case _ => Unit
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
