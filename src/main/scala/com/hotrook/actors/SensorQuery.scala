package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration.FiniteDuration

object SensorQuery {

  def props(
             sensorIdToActor: Map[String, ActorRef],
             requestId: Long,
             requester: ActorRef,
             timeout: FiniteDuration
           ): Props = {
    Props(new SensorQuery(sensorIdToActor, requestId, requester, timeout))
  }

  case object CollectionTimeout

}

class SensorQuery(
                   sensorIdToActor: Map[String, ActorRef],
                   requestId: Long,
                   requester: ActorRef,
                   timeout: FiniteDuration
                 ) extends Actor with ActorLogging {

  import context.dispatcher

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, SensorQuery.CollectionTimeout)

  override def preStart(): Unit = {
    sensorIdToActor.valuesIterator.foreach { sensor ⇒
      context.watch(sensor)
      sensor ! SensorDataStreamer.FinishProcessing(requester)
    }
  }

  override def postStop(): Unit = {
    queryTimeoutTimer.cancel()
  }

  override def receive: Receive = waitingForReplies(sensorIdToActor.values.toSet)


  def waitingForReplies(stillWaiting: Set[ActorRef]): Receive = {
    case trackMsg@Sensor.SensorSummary(_, _, _, _, _, _) =>
      val sensor = sender()
      requester ! trackMsg
      receivedResponse(sensor, stillWaiting)

    case SensorQuery.CollectionTimeout ⇒
      requester ! ResultsCollector.AllCollected
      context.stop(self)
  }

  private def receivedResponse(sensor: ActorRef, stillWaiting: Set[ActorRef]): Unit = {
    context.unwatch(sensor)
    val newStillWaiting = stillWaiting - sensor

    if (newStillWaiting.isEmpty) {
      requester ! ResultsCollector.AllCollected
      context.stop(self)
    } else {
      context.become(waitingForReplies(newStillWaiting))
    }

  }
}