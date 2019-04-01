package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object SensorManager {
  def props: Props = Props(new SensorManager)
}

class SensorManager extends Actor with ActorLogging {

  val sensorIdToActor = mutable.Map.empty[String, ActorRef]

  override def receive: Receive = {
    case trackMsg@SensorDataStreamer.SensorData(sensorId, _) =>
      sensorIdToActor.get(sensorId) match {
        case Some(sensor) =>
          sensor forward trackMsg
        case None =>
          val newSensor = context.actorOf(Sensor.props(sensorId), s"sensor-$sensorId")
          sensorIdToActor += sensorId -> newSensor
          newSensor forward trackMsg
      }
    case SensorDataStreamer.FinishProcessing(requester) =>
      context.actorOf(SensorQuery.props(sensorIdToActor.toMap, requester, 5 seconds))

  }
}
