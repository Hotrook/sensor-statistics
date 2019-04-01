package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.util.Try

object SensorDataStreamer {

  def props(dataManager: ActorRef): Props = Props(new SensorDataStreamer(dataManager))

  case class ProcessLine(line: String)

  case class SensorData(sensorId: String, temperature: Option[Int])

  case class FinishProcessing(resultsCollector: ActorRef)

}

class SensorDataStreamer(dataManager: ActorRef) extends Actor with ActorLogging {

  import SensorDataStreamer._

  override def receive : Receive = {

    case ProcessLine(line) =>
      val sensorData = processLine(line)
      dataManager ! sensorData

    case trackMsg@FinishProcessing(_) =>
      dataManager forward trackMsg

    case trackMsg@FileProcessor.EndOfFile(_) =>
      sender() ! trackMsg
  }

  private def processLine(line: String) = {
    val values = line.split(",")
    val sensorId = values(0)
    val temperature = Try(values(1).filterNot(_.isWhitespace).toInt).toOption
    SensorData(sensorId, temperature)
  }
}
