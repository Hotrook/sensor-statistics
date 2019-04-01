package com.hotrook.actors.printing

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class PrintManagerSpec() extends TestKit(ActorSystem("PrintManagerSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PrintManager" should {
    "correctly print all type of messages" in {
      val printer = TestProbe()
      val printManager = system.actorOf(PrintManager.props(printer.ref))

      supervisor.send(printManager, PrintManager.ProcessedFiles(10))
      printer.expectMsg(Printer.Print("Num of processed files: 10"))

      supervisor.send(printManager, PrintManager.ProcessedMeasurements(5))
      printer.expectMsg(Printer.Print("Num of processed measurements: 5"))

      supervisor.send(printManager, PrintManager.UnsuccessfulMeasurements(1000))
      printer.expectMsg(Printer.Print("Num of failed measurements: 1000"))

      supervisor.send(printManager, PrintManager.UnsuccessfulMeasurements(1000))
      printer.expectMsg(Printer.Print("Num of failed measurements: 1000"))

      supervisor.send(printManager, PrintManager.PrintResult)
      printer.expectMsg(Printer.Print("Sensors with highest avg humidity:"))
      printer.expectMsg(Printer.Print("sensor-id,min,avg,max"))

      supervisor.send(printManager, PrintManager.PrintResult("sensor1", Some(1), None, Some(414)))
      printer.expectMsg(Printer.Print("sensor1,1,414,NaN"))
    }
  }

}

/*
Num of processed files: 2
Num of processed measurements: 7
Num of failed measurements: 2

Sensors with highest avg humidity:

sensor-id,min,avg,max
s2,78,82,88
s1,10,54,98
s3,NaN,NaN,NaN
 */