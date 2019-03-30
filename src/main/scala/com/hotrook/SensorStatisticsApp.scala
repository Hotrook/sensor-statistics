package com.hotrook

import akka.actor.ActorSystem
import com.hotrook.actors.SensorStatisticsSupervisor

object SensorStatisticsApp {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("sensor-statistics")

    try {
      val supervisor = system.actorOf(SensorStatisticsSupervisor.props(), "sensor-statistics-supervisor")
    } finally {
      system.terminate()
    }
  }
}
