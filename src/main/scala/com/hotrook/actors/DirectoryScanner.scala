package com.hotrook.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object DirectoryScanner {

  def props(sensorDataStreamer: ActorRef): Props = Props(new DirectoryScanner(sensorDataStreamer))

  case class ScanDirectory(directoryPath: String)

  case class FileProcessorRegistered()

  case class FilesFound(numberOfFiles: Int)

  case class FilesLoaded(numberOfFiles: Int)

}

class DirectoryScanner(sensorDataStreamer: ActorRef) extends Actor with ActorLogging {

  import DirectoryScanner._

  override def receive: Receive = waitForScanning

  private def waitForScanning: Receive = {

    case ScanDirectory(directoryPath) =>
      log.info("Requested directory path to scan: {}", directoryPath)
      val files = scanDirectory(directoryPath)
      sender() ! FilesFound(files.size)

      val filesProcessors = files.map {
        file => context.actorOf(FileProcessor.props(self, sensorDataStreamer, file), s"FileScanner-${file.getName}")
      }

      filesProcessors.foreach(_ ! FileProcessor.LoadFile)

      if (files.isEmpty) {
        sender() ! DirectoryScanner.FilesLoaded(0)
        context stop self
      } else {
        context.become(waitingForResponses(filesProcessors, sender(), files.size))
      }
  }

  private def waitingForResponses(stillWaiting: Set[ActorRef], supervisor: ActorRef, size: Int): Receive = {

    case FileProcessor.FileLoaded(_) =>
      val fileProcessor = sender()
      receivedResponse(stillWaiting, fileProcessor, supervisor, size)
  }

  private def receivedResponse(stillWaiting: Set[ActorRef], fileProcessor: ActorRef, supervisor: ActorRef, size: Int): Unit = {
    val newStillWaiting = stillWaiting - fileProcessor

    if (newStillWaiting.isEmpty) {
      supervisor ! DirectoryScanner.FilesLoaded(size)
      context stop self
    } else {
      context.become(waitingForResponses(newStillWaiting, supervisor, size))
    }
  }

  private def scanDirectory(directoryPath: String): Set[File] = {
    val directory = new File(directoryPath)
    if (directory.exists() && directory.isDirectory) {
      directory.listFiles((_, name) => name.matches(""".+\.csv""")).toSet
    } else {
      Set()
    }
  }


}
