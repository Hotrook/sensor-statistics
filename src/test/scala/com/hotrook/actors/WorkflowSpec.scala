package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import com.hotrook.actors.printing.PrintManager
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class WorkflowSpec() extends TestKit(ActorSystem("WorkflowSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  val supervisor = TestProbe()
  val sensorId = "sensorId"
  val directory = "src/test/resources/testDirectories/3-files-dir-example"


  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Workflow" should {
    "work with DirectoryScanner, FileProcessors and SensorDataStreamer" in {
      val dataManager = TestProbe()
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(dataManager.ref))
      val directoryScanner = system.actorOf(DirectoryScanner.props(sensorDataStreamer))
      val supervisor = TestProbe()

      supervisor.send(directoryScanner, DirectoryScanner.ScanDirectory(directory))

      supervisor.expectMsg(DirectoryScanner.FilesFound(3))
      supervisor.expectMsg(DirectoryScanner.FilesLoaded(3))

      dataManager.expectMsgAllOf(
        SensorDataStreamer.SensorData("s1", Some(2)),
        SensorDataStreamer.SensorData("s2", Some(4)),
        SensorDataStreamer.SensorData("s2", Some(6)),
        SensorDataStreamer.SensorData("s3", Some(8)),
        SensorDataStreamer.SensorData("s3", Some(10)),
        SensorDataStreamer.SensorData("s1", Some(12)),
      )
    }

    "work with DirectoryScanner, FileProcessors, SensorDataStreamer and SensorManager" in {
      val sensorManager = system.actorOf(SensorManager.props)
      val resultCollector = TestProbe()
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(sensorManager))
      val directoryScanner = system.actorOf(DirectoryScanner.props(sensorDataStreamer))
      val supervisor = TestProbe()

      supervisor.send(directoryScanner, DirectoryScanner.ScanDirectory(directory))

      supervisor.expectMsg(DirectoryScanner.FilesFound(3))
      supervisor.expectMsg(DirectoryScanner.FilesLoaded(3))
      supervisor.send(sensorDataStreamer, SensorDataStreamer.FinishProcessing(resultCollector.ref))

      resultCollector.expectMsgAllOf(
        Sensor.SensorSummary("s1", Some(7), Some(2), Some(12), 2, 2),
        Sensor.SensorSummary("s2", Some(5), Some(4), Some(6), 2, 2),
        Sensor.SensorSummary("s3", Some(9), Some(8), Some(10), 2, 2)
      )
      resultCollector.expectMsg(ResultsCollector.AllCollected)
    }

    "work with DirectoryScanner, FileProcessors, SensorDataStreamer, SensorManager and ResultCollector" in {
      val printer = TestProbe()
      val supervisor = TestProbe()
      val sensorManager = system.actorOf(SensorManager.props)
      val resultCollector = system.actorOf(ResultsCollector.props(printer.ref, supervisor.ref))
      val sensorDataStreamer = system.actorOf(SensorDataStreamer.props(sensorManager))
      val directoryScanner = system.actorOf(DirectoryScanner.props(sensorDataStreamer))

      supervisor.send(directoryScanner, DirectoryScanner.ScanDirectory(directory))

      supervisor.expectMsg(DirectoryScanner.FilesFound(3))
      supervisor.expectMsg(DirectoryScanner.FilesLoaded(3))
      supervisor.send(sensorDataStreamer, SensorDataStreamer.FinishProcessing(resultCollector))

      printer.expectMsg(PrintManager.ProcessedMeasurements(6))
      printer.expectMsg(PrintManager.UnsuccessfulMeasurements(0))
      printer.expectMsg(PrintManager.PrintResults)
      printer.expectMsg(PrintManager.PrintResult("s3", Some(8), Some(9), Some(10)))
      printer.expectMsg(PrintManager.PrintResult("s1", Some(2), Some(7), Some(12)))
      printer.expectMsg(PrintManager.PrintResult("s2", Some(4), Some(5), Some(6)))
      printer.expectMsg(ResultsCollector.EndOfData)
    }
  }

}
