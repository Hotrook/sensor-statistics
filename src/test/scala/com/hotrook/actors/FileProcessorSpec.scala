package com.hotrook.actors


import java.io.File

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps


class FileProcessorSpec() extends TestKit(ActorSystem("FileProcessorSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "FileProcessor" should {
    "correctly process csv file" in {
      val supervisor = TestProbe()
      val sensorDataStreamer = TestProbe()
      val fileProcessor = system.actorOf(FileProcessor.props(supervisor.ref, sensorDataStreamer.ref))

      val file = new File("src/test/resources/example.csv")

      supervisor.send(fileProcessor, FileProcessor.LoadFile(file))

      sensorDataStreamer.expectMsg(SensorDataStreamer.ProcessLine("sensorId1,100"))
      sensorDataStreamer.expectMsg(SensorDataStreamer.ProcessLine("sensorId2,200"))
      sensorDataStreamer.expectMsg(SensorDataStreamer.ProcessLine("sensorId3,300"))
      sensorDataStreamer.expectMsg(SensorDataStreamer.ProcessLine("sensorId4,400"))
      val lastMessage = sensorDataStreamer.expectMsg(FileProcessor.EndOfFile("example.csv"))
      val lastSender = sensorDataStreamer.lastSender
      sensorDataStreamer.send(lastSender, lastMessage)

      supervisor.expectMsg(FileProcessor.FileLoaded("example.csv"))

    }
  }
}
