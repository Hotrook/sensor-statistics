package com.hotrook.actors


import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import com.hotrook.actors.printing.PrintManager
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class SensorStatisticsSupervisorSpec() extends TestKit(ActorSystem("SensorStatisticsSupervisorSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()
  val sensorId = "sensorId"

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SensorStatisticsSupervisor" should {
    "correctly initialize all actors and terminate system after all" in {
      val printManager = TestProbe()
      val directoryPath = "src/test/resources/testDirectories/3-files-dir-example"
      val supervisor = system.actorOf(SensorStatisticsSupervisor.props(directoryPath, printManager.ref))
      val watcher = TestProbe()

      supervisor ! SensorStatisticsSupervisor.Start

      printManager.expectMsg(PrintManager.ProcessedFiles(3))
      printManager.expectMsg(PrintManager.ProcessedMeasurements(6))
      printManager.expectMsg(PrintManager.UnsuccessfulMeasurements(0))
      printManager.expectMsg(PrintManager.PrintResults)
      printManager.expectMsg(PrintManager.PrintResult("s3", Some(8), Some(9), Some(10)))
      printManager.expectMsg(PrintManager.PrintResult("s1", Some(2), Some(7), Some(12)))
      printManager.expectMsg(PrintManager.PrintResult("s2", Some(4), Some(5), Some(6)))
      printManager.expectMsg(ResultsCollector.EndOfData)

      printManager.ref ! PoisonPill
    }
  }
}
