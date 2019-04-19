package akkaCluster

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.BroadcastPool

trait CreateRouter {
  this: Actor =>
  def createRouter: ActorRef = {
    context.actorOf(
      ClusterRouterPool(BroadcastPool(10), ClusterRouterPoolSettings(
        totalInstances = 100, maxInstancesPerNode = 20,
        allowLocalRoutees = false, useRole = None)).props(Props[Worker]),
      name = "worker-router")
  }
}