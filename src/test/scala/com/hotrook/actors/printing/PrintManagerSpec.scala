package com.hotrook.actors.printing

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import com.hotrook.actors.ResultsCollector
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

      supervisor.send(printManager, PrintManager.PrintResults)
      printer.expectMsg(Printer.Print("Sensors with highest avg humidity:"))
      printer.expectMsg(Printer.Print("sensor-id,min,avg,max"))

      supervisor.send(printManager, PrintManager.PrintResult("sensor1", Some(1), None, Some(414)))
      printer.expectMsg(Printer.Print("sensor1,1,414,NaN"))
    }

    "stop itself when EndOfData message is sent" in {
      val printer = TestProbe()
      val printManager = system.actorOf(PrintManager.props(printer.ref))

      supervisor.watch(printManager)
      supervisor.send(printManager, ResultsCollector.EndOfData)
      printer.expectMsg(ResultsCollector.EndOfData)
      printer.ref ! PoisonPill
      supervisor.expectTerminated(printManager)
    }
  }

}
