package com.hotrook.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.hotrook.actors.printing.PrintManager

object SensorStatisticsSupervisor {
  def props(directoryPath: String, printManager: ActorRef): Props =
    Props(new SensorStatisticsSupervisor(directoryPath, printManager))

  case object Start
  case object Stop
}

class SensorStatisticsSupervisor( directoryPath: String, printManager: ActorRef) extends Actor with ActorLogging {

  private val resultsCollector = context.actorOf(ResultsCollector.props(printManager, this.self))
  private val sensorManager = context.actorOf(SensorManager.props)
  private val sensorDataStreamer = context.actorOf(SensorDataStreamer.props(sensorManager))
  private val directoryScanner = context.actorOf(DirectoryScanner.props(sensorDataStreamer))

  override def preStart(): Unit = log.info("SensorStatistics app started")

  override def postStop(): Unit = log.info("SensorStatistics app stopped")

  override def receive: Receive = {

    case SensorStatisticsSupervisor.Start =>
      directoryScanner ! DirectoryScanner.ScanDirectory(directoryPath)

    case DirectoryScanner.FilesFound(numberOfFiles) =>
      log.info("Found {} files", numberOfFiles)

    case DirectoryScanner.FilesLoaded(numberOfFiles) =>
      log.info("Loaded {} files", numberOfFiles)
      printManager ! PrintManager.ProcessedFiles(numberOfFiles)
      sensorDataStreamer ! SensorDataStreamer.FinishProcessing(resultsCollector)

    case SensorStatisticsSupervisor.Stop =>
      context.system.terminate()
  }
}

