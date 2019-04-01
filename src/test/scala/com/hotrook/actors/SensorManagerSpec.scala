package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SensorManagerSpec extends TestKit(ActorSystem("SensorManagerSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()
  val requester = TestProbe()

  "SensorManager" should {
    "create 2 actor and forward messages to them" in {
      val sensorManager = system.actorOf(SensorManager.props)

      supervisor.send(sensorManager, SensorDataStreamer.SensorData("sensorId1", None))
      supervisor.send(sensorManager, SensorDataStreamer.SensorData("sensorId2", None))
      supervisor.send(sensorManager, SensorDataStreamer.FinishProcessing(requester.ref))

      requester.expectMsgClass(Sensor.SensorSummary("sensorId1", None, None, None, 1, 0).getClass)
      val sender1 = requester.lastSender

      requester.expectMsgClass(Sensor.SensorSummary("sensorId2", None, None, None, 1, 0).getClass)
      val sender2 = requester.lastSender

      requester.expectMsg(ResultsCollector.AllCollected)

      sensorManager should not equal sender1
      sensorManager should not equal sender2
      sender1 should not equal sender2
    }

    "not create second actor when forwarding messages with the same sensorId" in {
      val sensorManager = system.actorOf(SensorManager.props)
      val sensorId = "sensorId"

      supervisor.send(sensorManager, SensorDataStreamer.SensorData(sensorId, Some(2)))
      supervisor.send(sensorManager, SensorDataStreamer.SensorData(sensorId, None))

      supervisor.send(sensorManager, SensorDataStreamer.FinishProcessing(requester.ref))

      requester.expectMsg(Sensor.SensorSummary(sensorId, Some(1), Some(2), Some(2), 2, 1))
      val sender = requester.lastSender

      requester.expectMsg(ResultsCollector.AllCollected)

      sensorManager should not equal sender
    }
  }

}