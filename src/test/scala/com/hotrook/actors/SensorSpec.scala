package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class SensorSpec() extends TestKit(ActorSystem("SensorSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()
  val sensorId = "sensorId"

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Sensor" should {

    "return None values if all measurements were invalid" in {
      val sensor = system.actorOf(Sensor.props(sensorId))

      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, None))
      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, None))
      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, None))
      supervisor.send(sensor, SensorDataStreamer.FinishProcessing(supervisor.ref))

      supervisor.expectMsg(Sensor.SensorSummary(sensorId, None, None, None, 3, 0))
    }

    "not process message if incorrect sensor id" in {
      val sensor = system.actorOf(Sensor.props(sensorId))

      supervisor.send(sensor, SensorDataStreamer.SensorData("randomId", None))
      supervisor.send(sensor, SensorDataStreamer.FinishProcessing(supervisor.ref))

      supervisor.expectMsg(Sensor.SensorSummary(sensorId, None, None, None, 0, 0))
    }

    "calculate correct min, max and avg" in {
      val sensor = system.actorOf(Sensor.props(sensorId))

      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, Some(1)))
      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, Some(2)))
      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, Some(3)))
      supervisor.send(sensor, SensorDataStreamer.FinishProcessing(supervisor.ref))

      supervisor.expectMsg(Sensor.SensorSummary(sensorId, Some(2), Some(1), Some(3), 3, 3))
    }

    "should include unsuccessful measurements to counting average" in {
      val sensor = system.actorOf(Sensor.props(sensorId))

      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, None))
      supervisor.send(sensor, SensorDataStreamer.SensorData(sensorId, Some(2)))
      supervisor.send(sensor, SensorDataStreamer.FinishProcessing(supervisor.ref))

      supervisor.expectMsg(Sensor.SensorSummary(sensorId, Some(1), Some(2), Some(2), 2, 1))
    }
  }
}
