package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, Props}

object SensorStatisticsSupervisor {
  def props(): Props = Props(new SensorStatisticsSupervisor)
}

class SensorStatisticsSupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("SensorStatistics app started")

  override def postStop(): Unit = log.info("SensorStatistics app stopped")

  override def receive = Actor.emptyBehavior
}

