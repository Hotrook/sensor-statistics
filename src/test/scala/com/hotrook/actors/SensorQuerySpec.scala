package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class SensorQuerySpec() extends TestKit(ActorSystem("SensorQuerySpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()
  val sensorId = "sensorId"

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SensorQuery" should {
    "should collect all requests in time" in {
      val sensor1 = TestProbe()
      val sensor2 = TestProbe()
      val requester = TestProbe()
      val sensorIdToActorRef = Map(
        "sensorId1" -> sensor1.ref,
        "sensorId2" -> sensor2.ref
      )
      system.actorOf(SensorQuery.props(sensorIdToActorRef, 1, requester.ref, 2 seconds))
      val firstReply = Sensor.SensorSummary("sensorId1", None, None, None, 0, 0)
      val secondReply = Sensor.SensorSummary("sensorId2", None, None, None, 0, 0)

      sensor1.expectMsg(SensorDataStreamer.FinishProcessing(requester.ref))
      sensor1.send(sensor1.lastSender, firstReply)

      sensor2.expectMsg(SensorDataStreamer.FinishProcessing(requester.ref))
      sensor2.send(sensor2.lastSender, secondReply)

      requester.expectMsg(firstReply)
      requester.expectMsg(secondReply)
      requester.expectMsg(ResultsCollector.AllCollected)
    }
  }

  "should timeout and collect one message less when one of the sensors does not respond" in {
    val sensor = TestProbe()
    val requester = TestProbe()
    val sensorIdToActorRef = Map(
      "sensorId1" -> sensor.ref,
      "sensorId2" -> TestProbe().ref
    )

    system.actorOf(SensorQuery.props(sensorIdToActorRef, 1, requester.ref, 2 seconds))
    val reply = Sensor.SensorSummary("sensorId1", None, None, None, 0, 0)

    sensor.expectMsg(SensorDataStreamer.FinishProcessing(requester.ref))
    sensor.send(sensor.lastSender, reply)


    requester.expectMsg(reply)
    requester.expectMsg(ResultsCollector.AllCollected)
  }

}
