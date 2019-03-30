package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class SensorDataStreamerSpec(_system: ActorSystem) extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("DirectoryScannerSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "SensorDataStreamer" should {
    "forwards stop message to dataManager" in {
      val supervisor = TestProbe()
      val dataManager = TestProbe()
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.FinishProcessing)

      dataManager.expectMsg(SensorDataStreamer.FinishProcessing)
    }

    "correctly process valid line" in {
      val supervisor = TestProbe()
      val dataManager = TestProbe()
      val sensorId = "sensorId"
      val temperature = 100.toString
      val line = sensorId + "," + temperature
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.ProcessLine(line))

      dataManager.expectMsg(SensorDataStreamer.SensorData(sensorId, Some(100)))
    }

    "correctly process line with white characters" in {
      val supervisor = TestProbe()
      val dataManager = TestProbe()
      val sensorId = "sensor Id"
      val temperature = 100.toString
      val line = sensorId + ", " + temperature + " "
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.ProcessLine(line))

      dataManager.expectMsg(SensorDataStreamer.SensorData(sensorId, Some(100)))
    }

    "correctly process line with NaN" in {
      val supervisor = TestProbe()
      val dataManager = TestProbe()
      val sensorId = "sensorId"
      val temperature = "NaN"
      val line = sensorId + "," + temperature
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.ProcessLine(line))

      dataManager.expectMsg(SensorDataStreamer.SensorData(sensorId, None))
    }
  }

}