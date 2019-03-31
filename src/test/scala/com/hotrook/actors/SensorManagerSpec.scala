package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class SensorManagerSpec extends TestKit(ActorSystem("SensorManagerSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()

  "SensorManager" should {
    "create 2 actor and forward messages to them" in {
      val sensorManager = system.actorOf(SensorManager.props)

      supervisor.send(sensorManager, SensorDataStreamer.SensorData("sensorId1", None))
      supervisor.send(sensorManager, SensorDataStreamer.SensorData("sensorId2", None))

      supervisor.send(sensorManager, SensorDataStreamer.FinishProcessing)

      supervisor.expectMsgClass(Sensor.SensorSummary("sensorId1", None, None, None, 1, 0).getClass)
      val sender1 = supervisor.lastSender

      supervisor.expectMsg(Sensor.SensorSummary("sensorId2", None, None, None, 1, 0).getClass)
      val sender2 = supervisor.lastSender

      sensorManager should not equal sender1
      sensorManager should not equal sender2
      sender1 should not equal sender2
    }

    "not create second actor when forwarding messages with the same sensorId" in {
      val sensorManager = system.actorOf(SensorManager.props)
      val sensorId = "sensorId"

      supervisor.send(sensorManager, SensorDataStreamer.SensorData(sensorId, Some(2)))
      supervisor.send(sensorManager, SensorDataStreamer.SensorData(sensorId, None))

      supervisor.send(sensorManager, SensorDataStreamer.FinishProcessing)

      supervisor.expectMsg(Sensor.SensorSummary(sensorId, Some(1), Some(2), Some(2), 2, 1))

      val sender = supervisor.lastSender

      supervisor.expectNoMessage(1 second)

      sensorManager should not equal sender
    }
  }

}