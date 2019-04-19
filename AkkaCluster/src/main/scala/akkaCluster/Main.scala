package akkaCluster

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import akkaCluster.WordCounter.JobRequest
import com.typesafe.config.ConfigFactory

import scala.io.Source

object Main extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("wordCountCluster", config)

  println(s"Node Role ${Cluster(system).selfRoles} started")

  if (system.settings.config.getStringList("akka.cluster.roles").contains("master")) {
    Cluster(system).registerOnMemberUp {
      val wordCounter = system.actorOf(Props[WordCounter], "receptionist")
      val lines = readFile("inputFile.txt").toList
      val linesPerJob = 20
      wordCounter ! JobRequest("Job", lines, linesPerJob)
    }
  }

  def readFile(filename: String): Seq[String] = {
    val bufferedSource = Source.fromFile(filename)
    val lines = (for (line <- bufferedSource.getLines()) yield line).toList
    bufferedSource.close
    lines
  }

}