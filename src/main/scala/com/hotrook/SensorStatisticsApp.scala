package com.hotrook

import akka.actor.ActorSystem
import com.hotrook.actors._
import com.hotrook.actors.printing.{PrintManager, Printer}
import com.typesafe.config.ConfigFactory

object SensorStatisticsApp {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem("sensor-statistics")
    val directoryPath = ConfigFactory.load().getString("app.directoryPath")
    val printer = system.actorOf(Printer.props)
    val printManager = system.actorOf(PrintManager.props(printer))
    val supervisor = system.actorOf(SensorStatisticsSupervisor.props(directoryPath, printManager),
      "sensor-statistics-supervisor")

    supervisor ! SensorStatisticsSupervisor.Start
  }
}
