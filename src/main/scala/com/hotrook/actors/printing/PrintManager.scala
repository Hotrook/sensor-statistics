package com.hotrook.actors.printing

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.hotrook.actors.ResultsCollector

object PrintManager {
  def props(printer: ActorRef): Props = Props(new PrintManager(printer))

  sealed class PrintMessage

  case class ProcessedFiles(number: Int) extends PrintMessage

  case class ProcessedMeasurements(number: Int) extends PrintMessage

  case class UnsuccessfulMeasurements(number: Int) extends PrintMessage

  case class PrintResult(sensorId: String, maxTemperature: Option[Int], average: Option[Int], minTemperature: Option[Int]) extends PrintMessage

  case object PrintResults extends PrintMessage

}

class PrintManager(printer: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {

    case PrintManager.ProcessedFiles(number) =>
      printer ! Printer.Print(s"Num of processed files: $number")

    case PrintManager.ProcessedMeasurements(number) =>
      printer ! Printer.Print(s"Num of processed measurements: $number")

    case PrintManager.UnsuccessfulMeasurements(number) =>
      printer ! Printer.Print(s"Num of failed measurements: $number")

    case PrintManager.PrintResults =>
      printer ! Printer.Print("Sensors with highest avg humidity:")
      printer ! Printer.Print("sensor-id,min,avg,max")

    case PrintManager.PrintResult(sensorId, max, average, min) =>
      val maxToPrint = max.map(_.toString).getOrElse("NaN")
      val minToPrint = min.map(_.toString).getOrElse("NaN")
      val averageToPrint = average.map(_.toString).getOrElse("NaN")
      printer ! Printer.Print(s"$sensorId,$maxToPrint,$minToPrint,$averageToPrint")

    case msg@ResultsCollector.EndOfData =>
      context watch printer
      printer ! msg

    case Terminated(`printer`) =>
      context stop self
  }
}
