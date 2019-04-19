package akkaCluster

import akka.actor._
import akkaCluster.Master.StartJob
import akkaCluster.WordCounter._

object WordCounter {

  trait Response
  case class JobSuccess(name: String, map: Map[String, Int]) extends Response
  case class JobFailure(name: String) extends Response
  case class JobRequest(name: String, text: List[String], size: Int)
  case class WordCount(name: String, map: Map[String, Int])
  case class Job(name: String, text: List[String], respondTo: ActorRef, jobMaster: ActorRef)

}

class WordCounter extends Actor with ActorLogging with CreateMaster {

  import context._

  var jobs = Set[Job]()

  def receive = {

    case JobRequest(name, text, size) =>
      log.info(s"Requested job: $name")
      val jobMaster = createMaster(s"master-$name")
      val job = Job(name, text, sender, jobMaster)
      jobs = jobs + job
      jobMaster ! StartJob(name, text, size)
      watch(jobMaster)

    case WordCount(jobName, map) =>
      log.info(s"Job $jobName completed \n Word Count result is :${map}")
      jobs.find(_.name == jobName).foreach { job =>
        job.respondTo ! JobSuccess(jobName, map)
        stop(job.jobMaster)
        jobs = jobs - job
      }

  }

}
