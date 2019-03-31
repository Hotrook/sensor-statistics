package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
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
      val resultsCollector = system.actorOf(ResultsCollector.props(printer.ref))

      supervisor.send(resultsCollector, Sensor.SensorSummary(Some(1), Some(0), Some(2), 5, 4))
      supervisor.send(resultsCollector, Sensor.SensorSummary(Some(4), Some(3), Some(5), 5, 4))
      supervisor.send(resultsCollector, Sensor.SensorSummary(None, None, None, 5, 0))
      supervisor.send(resultsCollector, Sensor.SensorSummary(Some(2), Some(1), Some(3), 5, 4))
      supervisor.send(resultsCollector, Sensor.SensorSummary(Some(3), Some(2), Some(4), 5, 4))
      supervisor.send(resultsCollector, ResultsCollector.AllCollected)

      printer.expectMsg(ResultsCollector.ProcessedMeasurements(25))
      printer.expectMsg(ResultsCollector.UnsuccessfulMeasurements(9))
      printer.expectMsg(ResultsCollector.PrintResults)
      printer.expectMsg(ResultsCollector.PrintResult(Some(3), Some(4), Some(5)))
      printer.expectMsg(ResultsCollector.PrintResult(Some(2), Some(3), Some(4)))
      printer.expectMsg(ResultsCollector.PrintResult(Some(1), Some(2), Some(3)))
      printer.expectMsg(ResultsCollector.PrintResult(Some(0), Some(1), Some(2)))
      printer.expectMsg(ResultsCollector.PrintResult(None, None, None))
    }

    "not crash if 0 summaries submitted" in {
      val printer = TestProbe()
      val resultsCollector = system.actorOf(ResultsCollector.props(printer.ref))

      supervisor.send(resultsCollector, ResultsCollector.AllCollected)

      printer.expectMsg(ResultsCollector.ProcessedMeasurements(0))
      printer.expectMsg(ResultsCollector.UnsuccessfulMeasurements(0))
      printer.expectMsg(ResultsCollector.PrintResults)
      printer.expectNoMessage(1 second)
    }
  }
}