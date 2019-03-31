package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.hotrook.actors.SensorDataStreamer.FinishProcessing

import scala.math.{max, min}

object Sensor {
  def props(sensorId: String): Props = Props(new Sensor(sensorId))

  case class SensorSummary(
                            average: Option[Int],
                            minTemperature: Option[Int],
                            mxaTemperature: Option[Int],
                            numberOfRequests: Int,
                            successfulRequests: Int
                          )

}

class Sensor(sensorId: String) extends Actor with ActorLogging {

  import Sensor._

  var sum: Option[Int] = None
  var minTemperature: Option[Int] = None
  var maxTemperature: Option[Int] = None
  var numberOfRequests = 0
  var successfulRequests = 0

  override def receive = {
    case SensorDataStreamer.SensorData(`sensorId`, temperature) =>
      saveRecord(temperature)
    case FinishProcessing =>
      sender ! createSummaryMessage()
      context stop self
  }

  private def createSummaryMessage() = {
    val average = sum.map(_ / numberOfRequests)
    SensorSummary(average, minTemperature, maxTemperature, numberOfRequests, successfulRequests)
  }

  private def saveRecord(temperature: Option[Int]): Unit = {
    successfulRequests += temperature.map(_ => 1).getOrElse(0)
    numberOfRequests += 1
    sum = calculateNewValue(sum, temperature, _ + _)
    minTemperature = calculateNewValue(minTemperature, temperature, min)
    maxTemperature = calculateNewValue(maxTemperature, temperature, max)
  }

  private def calculateNewValue(oldValue: Option[Int], newValue: Option[Int], reducer: (Int, Int) => Int): Option[Int] = {
    (oldValue ++ newValue).reduceOption(reducer)
  }

}