package com.hotrook

import akka.actor.ActorSystem
import com.hotrook.actors._
import com.hotrook.actors.printing.{PrintManager, Printer}

object SensorStatisticsApp {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem("sensor-statistics")

    val printer = system.actorOf(Printer.props)
    val printManager = system.actorOf(PrintManager.props(printer))

    val supervisor = system.actorOf(SensorStatisticsSupervisor.props(
      "src/test/resources/testDirectories/3-files-dir-example", printManager), "sensor-statistics-supervisor")
    supervisor ! SensorStatisticsSupervisor.Start
  }
}
