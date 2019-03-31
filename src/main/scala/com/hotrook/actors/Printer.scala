package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, Props}

object Printer {
  def props: Props = Props(new Printer)


  sealed class PrintMessage

  case class ProcessedFiles(number: Int) extends PrintMessage

  case class ProcessedMeasurements(number: Int) extends PrintMessage

  case class UnsuccessfulMeasurements(number: Int) extends PrintMessage

  case class PrintResult(sensorId: String, maxTemperature: Option[Int], average: Option[Int], minTemperature: Option[Int]) extends PrintMessage

  case object PrintResults extends PrintMessage

}

class Printer extends Actor with ActorLogging {
  override def receive: Receive = {
    case Printer.ProcessedFiles(number) =>
      println("Num of processed files: {}", number)
    case Printer.ProcessedMeasurements(number) =>
      println("Num of processed measurements: {}", number)
    case Printer.UnsuccessfulMeasurements(number) =>
      println("Num of failed measurements: {}", number)
    case Printer.PrintResult =>
      println("sensor-id,min,avg,max")
    case Printer.PrintResult(sensorId, max, average, min) =>
      println("{},{},{},{}",
        sensorId,
        max.map(_.toString).getOrElse("NaN"),
        average.map(_.toString).getOrElse("NaN"),
        min.map(_.toString).getOrElse("NaN")
      )

  }
}
