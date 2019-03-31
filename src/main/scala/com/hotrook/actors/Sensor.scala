package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.hotrook.actors.SensorDataStreamer.FinishProcessing

import scala.math.{max, min}

object Sensor {
  def props(sensorId: String): Props = Props(new Sensor(sensorId))

  case class SensorSummary(
                            sensorId: String,
                            average: Option[Int],
                            minTemperature: Option[Int],
                            maxTemperature: Option[Int],
                            numberOfRequests: Int,
                            successfulRequests: Int
                          )

}

class Sensor(sensorId: String) extends Actor with ActorLogging {

  import Sensor._

  override def receive: Receive = waitingForMeasurements(State(None, None, None, 0, 0))

  private def waitingForMeasurements(state: State): Receive = {
    case SensorDataStreamer.SensorData(`sensorId`, temperature) =>
      saveRecord(temperature, state)
    case FinishProcessing =>
      sender ! createSummaryMessage(state)
      context stop self
  }

  private def createSummaryMessage(state: State) = {
    val average = state.sum.map(_ / state.numberOfRequests)
    SensorSummary(
      sensorId,
      average,
      state.minTemperature,
      state.maxTemperature,
      state.numberOfRequests,
      state.successfulRequests
    )
  }

  private def saveRecord(temperature: Option[Int], oldState: State): Unit = {
    context.become(waitingForMeasurements(State(
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

  private case class State(
                            sum: Option[Int],
                            minTemperature: Option[Int],
                            maxTemperature: Option[Int],
                            numberOfRequests: Int,
                            successfulRequests: Int
                          )

}