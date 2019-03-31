package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.math.Ordered.orderingToOrdered

object ResultsCollector {
  def props(printer: ActorRef): Props = Props(new ResultsCollector(printer))

  case class CollectResults(numberOfResults: Int)

  case object AllCollected

}

class ResultsCollector(printer: ActorRef) extends Actor with ActorLogging {

  val results: mutable.Set[Sensor.SensorSummary] = mutable.Set()

  override def receive: Receive = {
    case summary@Sensor.SensorSummary(_, _, _, _, _, _) =>
      results += summary
    case ResultsCollector.AllCollected =>
      makeReport()
  }

  private def makeReport() = {
    val processedMeasurements = results.foldLeft(0)(_ + _.numberOfRequests)
    printer ! Printer.ProcessedMeasurements(processedMeasurements)

    val successfulMeasurements = results.foldLeft(0)(_ + _.successfulRequests)
    val unsuccessfulMeasurements = processedMeasurements - successfulMeasurements
    printer ! Printer.UnsuccessfulMeasurements(unsuccessfulMeasurements)

    printer ! Printer.PrintResults

    val sortedResults = results.toSeq.sortWith(_.average > _.average)
    sortedResults.foreach { x =>
      printer ! Printer.PrintResult(x.sensorId, x.minTemperature, x.average, x.maxTemperature)
    }
  }

}