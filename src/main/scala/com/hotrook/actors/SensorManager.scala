package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable.Map

object SensorManager {
  def props: Props = Props(new SensorManager)
}

class SensorManager extends Actor with ActorLogging {

  val sensorIdToActor = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case trackMsg@SensorDataStreamer.SensorData(sensorId, _) =>
      sensorIdToActor.get(sensorId) match {
        case Some(sensor) =>
          sensor forward trackMsg
        case None =>
          val newSensor = context.actorOf(Sensor.props(sensorId), s"sensor-${sensorId}")
          sensorIdToActor += sensorId -> newSensor
          newSensor forward trackMsg
      }
    case trackMsg@SensorDataStreamer.FinishProcessing(_) =>
      sensorIdToActor.foreach {
        case (_, sensor) => sensor forward trackMsg
      }
  }
}