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

  override def receive: Receive = waitingForMeasurements(SensorState(None, None, None, 0, 0))

  def waitingForMeasurements(state: SensorState): Receive = {
    case SensorDataStreamer.SensorData(`sensorId`, temperature) =>
      saveRecord(temperature, state)
    case FinishProcessing =>
      sender ! createSummaryMessage(state)
      context stop self
  }

  private def createSummaryMessage(state: SensorState) = {
    val average = state.sum.map(_ / state.numberOfRequests)
    SensorSummary(
      average,
      state.minTemperature,
      state.maxTemperature,
      state.numberOfRequests,
      state.successfulRequests
    )
  }

  private def saveRecord(temperature: Option[Int], oldState: SensorState): Unit = {
    context.become(waitingForMeasurements(SensorState(
      calculateNewValue(oldState.sum, temperature, _ + _),
      calculateNewValue(oldState.minTemperature, temperature, min),
      calculateNewValue(oldState.maxTemperature, temperature, max),
      oldState.numberOfRequests + 1,
      oldState.successfulRequests + temperature.map(_ => 1).getOrElse(0)
    )))
  }

  private def calculateNewValue(oldValue: Option[Int], newValue: Option[Int], reducer: (Int, Int) => Int): Option[Int] = {
    (oldValue ++ newValue).reduceOption(reducer)
  }

  private case class SensorState(
                                  sum: Option[Int],
                                  minTemperature: Option[Int],
                                  maxTemperature: Option[Int],
                                  numberOfRequests: Int,
                                  successfulRequests: Int
                                )

}