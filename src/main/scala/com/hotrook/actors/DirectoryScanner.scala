package com.hotrook.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.None

object DirectoryScanner {
  def props(supervisor: ActorRef): Props = Props(new DirectoryScanner(supervisor))

  case class ScanDirectory(directoryPath: String)
  case class FileProcessorRegistered()

  case class FilesFound(numberOfFiles: Int)

}

class DirectoryScanner(supervisor: ActorRef) extends Actor with ActorLogging {

  import DirectoryScanner._

  override def receive = {
    case ScanDirectory(directoryPath) =>
      log.info("Requested directory path to scan: {}", directoryPath)
      val files = scanDirectory(directoryPath)

      sender() ! FilesFound(files.size)

      files.foreach(file => {
        val deviceActor = context.actorOf(FileProcessor.props, s"FileScanner-${file.getName}")
        deviceActor forward FileProcessor.LoadFile(file)
      })
  }

  private def scanDirectory(directoryPath: String): List[File] = {
    val directory = new File(directoryPath)
    directory match {
      case null => List()
      case _ => directory.listFiles().filter(_.isFile).toList
    }
//    if (directory == null)
//      List()
//    else
//      directory.listFiles().filter(_.isFile).toList
  }


}
