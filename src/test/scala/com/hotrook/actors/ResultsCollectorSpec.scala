package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.hotrook.actors.printing.PrintManager
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps


class ResultsCollectorSpec() extends TestKit(ActorSystem("ResultsCollectorSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ResultsCollector" should {
    "sort summaries from biggest average temperature" in {
      val printer = TestProbe()
      val resultsCollector = system.actorOf(ResultsCollector.props(printer.ref, supervisor.ref))

      supervisor.send(resultsCollector, Sensor.SensorSummary("s4", Some(1), Some(0), Some(2), 5, 4))
      supervisor.send(resultsCollector, Sensor.SensorSummary("s1", Some(4), Some(3), Some(5), 5, 4))
      supervisor.send(resultsCollector, Sensor.SensorSummary("s5", None, None, None, 5, 0))
      supervisor.send(resultsCollector, Sensor.SensorSummary("s3", Some(2), Some(1), Some(3), 5, 4))
      supervisor.send(resultsCollector, Sensor.SensorSummary("s2", Some(3), Some(2), Some(4), 5, 4))
      supervisor.send(resultsCollector, ResultsCollector.AllCollected)

      printer.expectMsg(PrintManager.ProcessedMeasurements(25))
      printer.expectMsg(PrintManager.UnsuccessfulMeasurements(9))
      printer.expectMsg(PrintManager.PrintResults)
      printer.expectMsg(PrintManager.PrintResult("s1", Some(3), Some(4), Some(5)))
      printer.expectMsg(PrintManager.PrintResult("s2", Some(2), Some(3), Some(4)))
      printer.expectMsg(PrintManager.PrintResult("s3", Some(1), Some(2), Some(3)))
      printer.expectMsg(PrintManager.PrintResult("s4", Some(0), Some(1), Some(2)))
      printer.expectMsg(PrintManager.PrintResult("s5", None, None, None))
      printer.expectMsg(ResultsCollector.EndOfData)
    }

    "not crash if 0 summaries submitted" in {
      val printer = TestProbe()
      val resultsCollector = system.actorOf(ResultsCollector.props(printer.ref, supervisor.ref))

      supervisor.send(resultsCollector, ResultsCollector.AllCollected)

      printer.expectMsg(PrintManager.ProcessedMeasurements(0))
      printer.expectMsg(PrintManager.UnsuccessfulMeasurements(0))
      printer.expectMsg(PrintManager.PrintResults)
      printer.expectMsg(ResultsCollector.EndOfData)

      printer.expectNoMessage(1 second)
    }
  }
}