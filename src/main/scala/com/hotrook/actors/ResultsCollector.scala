package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.math.Ordered.orderingToOrdered

object ResultsCollector {
  def props(printer: ActorRef): Props = Props(new ResultsCollector(printer))

  case class CollectResults(numberOfResults: Int)

  case class ProcessedMeasurements(number: Int)

  case class UnsuccessfulMeasurements(number: Int)

  case class PrintResult(mxaTemperature: Option[Int], average: Option[Int], minTemperature: Option[Int])

  case object PrintResults

  case object AllCollected

}

class ResultsCollector(printer: ActorRef) extends Actor with ActorLogging {

  val results: mutable.Set[Sensor.SensorSummary] = mutable.Set()

  override def receive: Receive = {
    case summary@Sensor.SensorSummary(_, _, _, _, _) =>
      results += summary
    case ResultsCollector.AllCollected =>
      makeReport()
  }

  private def makeReport() = {
    val processedMeasurements = results.foldLeft(0)(_ + _.numberOfRequests)
    printer ! ResultsCollector.ProcessedMeasurements(processedMeasurements)

    val successfulMeasurements = results.foldLeft(0)(_ + _.successfulRequests)
    val unsuccessfulMeasurements = processedMeasurements - successfulMeasurements
    printer ! ResultsCollector.UnsuccessfulMeasurements(unsuccessfulMeasurements)

    printer ! ResultsCollector.PrintResults

    val sortedResults = results.toSeq.sortWith(_.average > _.average)
    sortedResults.foreach { x =>
      printer ! ResultsCollector.PrintResult(x.minTemperature, x.average, x.maxTemperature)
    }
  }

}