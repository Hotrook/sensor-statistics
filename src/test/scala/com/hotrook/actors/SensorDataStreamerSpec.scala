package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class SensorDataStreamerSpec() extends TestKit(ActorSystem("DirectoryScannerSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()
  val dataManager = TestProbe()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SensorDataStreamer" should {
    "forwards stop message to dataManager" in {
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.FinishProcessing)

      dataManager.expectMsg(SensorDataStreamer.FinishProcessing)
    }

    "correctly process valid line" in {
      val sensorId = "sensorId"
      val temperature = 100.toString
      val line = createLine(sensorId, temperature)
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.ProcessLine(line))

      dataManager.expectMsg(SensorDataStreamer.SensorData(sensorId, Some(100)))
    }

    "correctly process line with white characters" in {
      val sensorId = "sensor Id"
      val temperature = " " + 100.toString + " "
      val line = createLine(sensorId, temperature)
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.ProcessLine(line))

      dataManager.expectMsg(SensorDataStreamer.SensorData(sensorId, Some(100)))
    }

    "correctly process line with NaN" in {
      val sensorId = "sensorId"
      val temperature = "NaN"
      val line = createLine(sensorId, temperature)
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))

      supervisor.send(sensorDataStreamer, SensorDataStreamer.ProcessLine(line))

      dataManager.expectMsg(SensorDataStreamer.SensorData(sensorId, None))
    }
  }

  private def createLine(sensorId: String, temperature: String) = {
    sensorId + "," + temperature
  }
}