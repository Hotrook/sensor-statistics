package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.hotrook.actors.printing.PrintManager

import scala.collection.mutable
import scala.math.Ordered.orderingToOrdered

object ResultsCollector {
  def props(printer: ActorRef, supervisor: ActorRef): Props = Props(new ResultsCollector(printer, supervisor))

  case class CollectResults(numberOfResults: Int)

  case object AllCollected

  case object EndOfData
}

class ResultsCollector(printer: ActorRef, supervisor: ActorRef) extends Actor with ActorLogging {

  val results: mutable.Set[Sensor.SensorSummary] = mutable.Set()

  override def receive: Receive = {

    case summary@Sensor.SensorSummary(_, _, _, _, _, _) =>
      results += summary

    case ResultsCollector.AllCollected =>
      makeReport()

    case Terminated(`printer`) =>
      supervisor ! ResultsCollector.EndOfData
      context stop self
  }

  private def makeReport() = {
    context watch printer

    val processedMeasurements = results.foldLeft(0)(_ + _.numberOfRequests)
    printer ! PrintManager.ProcessedMeasurements(processedMeasurements)

    val successfulMeasurements = results.foldLeft(0)(_ + _.successfulRequests)
    val unsuccessfulMeasurements = processedMeasurements - successfulMeasurements
    printer ! PrintManager.UnsuccessfulMeasurements(unsuccessfulMeasurements)

    printer ! PrintManager.PrintResults

    val sortedResults = results.toSeq.sortWith(_.average > _.average)
    sortedResults.foreach { x =>
      printer ! PrintManager.PrintResult(x.sensorId, x.minTemperature, x.average, x.maxTemperature)
    }

    printer ! ResultsCollector.EndOfData
  }

}