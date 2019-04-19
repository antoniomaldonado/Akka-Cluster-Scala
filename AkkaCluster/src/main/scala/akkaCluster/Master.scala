package akkaCluster

import akka.actor._
import akkaCluster.Master._
import akkaCluster.Worker._
import akkaCluster.WordCounter.WordCount

import scala.concurrent.duration._

object Master {

  case class StartJob(name: String, text: List[String], size: Int)
  case class EnQueue(worker: ActorRef)
  case object NextTask
  case class CompletedTask(map: Map[String, Int])
  case object MergeResults

}

class Master extends Actor with ActorLogging with CreateRouter {

  import context._

  val router = createRouter

  var textLines = Vector[List[String]]()
  var partialCounts = Vector[Map[String, Int]]()
  var newTasks = 0
  var completedTasks = 0
  var workers = Set[ActorRef]()


  def receive = {
    case StartJob(jobName, text, size) =>
      textLines = text.grouped(size).toVector
      become(
        working(
          jobName,
          sender,
          context.system.scheduler.schedule(0 millis, 1000 millis, router, Work(jobName, self))
        ))
  }

  def working(jobName: String, wordCounter: ActorRef, cancellable: Cancellable): Receive = {
    case EnQueue(worker) =>
      watch(worker)
      workers = workers + worker
    case NextTask =>
      if (textLines.isEmpty)
        sender() ! EmptyWorkLoad
      else {
        sender() ! Task(textLines.head, self)
        newTasks += 1
        textLines = textLines.tail
      }
    case CompletedTask(countMap) =>
      partialCounts = partialCounts :+ countMap
      completedTasks += 1
      if (textLines.isEmpty && newTasks == completedTasks) {
        cancellable.cancel()
        become(mergePartialCounts(jobName, wordCounter, workers))
        self ! MergeResults
      }
    case Terminated(worker) =>
      log.info(s"Worker $worker Terminated. Stopping $jobName.")
      stop(self)
  }

  def mergePartialCounts(jobName: String, wordCounter: ActorRef, workers: Set[ActorRef]): Receive = {
    case MergeResults =>
      val mergedMap = partialCounts.foldLeft(Map[String, Int]()) {
        (mergedCount: Map[String, Int], accumulator) =>
          mergedCount.map {
            case (word, count) =>
              accumulator.get(word).map(accCount => (word -> (accCount + count))).getOrElse(word -> count)
          } ++ (accumulator -- mergedCount.keys)
      }
      workers.foreach(stop(_))
      wordCounter ! WordCount(jobName, mergedMap)
  }

}
