package akkaCluster

import akka.actor.{ActorContext, Props}

trait CreateMaster {
  def context: ActorContext
  def createMaster(name: String) = context.actorOf(Props(new Master), name)
}
