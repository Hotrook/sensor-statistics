package com.hotrook.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object DirectoryScanner {
  def props(sensorDataStreamer: ActorRef): Props = Props(new DirectoryScanner(sensorDataStreamer))

  case class ScanDirectory(directoryPath: String)

  case class FileProcessorRegistered()

  case class FilesFound(numberOfFiles: Int)

}

class DirectoryScanner(sensorDataStreamer: ActorRef) extends Actor with ActorLogging {

  import DirectoryScanner._

  override def receive: Receive = {
    case ScanDirectory(directoryPath) =>
      log.info("Requested directory path to scan: {}", directoryPath)
      val files = scanDirectory(directoryPath)

      sender() ! FilesFound(files.size)

      files.foreach(file => {
        val deviceActor = context.actorOf(FileProcessor.props(sender(), sensorDataStreamer), s"FileScanner-${file.getName}")
        deviceActor forward FileProcessor.LoadFile(file)
      })
  }

  private def scanDirectory(directoryPath: String): List[File] = {
    val directory = new File(directoryPath)
    if (directory.exists() && directory.isDirectory) {
      directory.listFiles((_, name) => name.matches(""".+\.csv""")).toList
    } else {
      List()
    }
  }


}
