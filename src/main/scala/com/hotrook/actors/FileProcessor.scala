package com.hotrook.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.io.Source

object FileProcessor {
  def props(supervisor: ActorRef, sensorDataStreamer: ActorRef, file: File): Props = Props(new FileProcessor(
    supervisor,
    sensorDataStreamer,
    file
  ))

  case object LoadFile

  case class FileLoaded(files: String)

  case class EndOfFile(filename: String)

}

class FileProcessor(supervisor: ActorRef, sensorDataStreamer: ActorRef, file: File) extends Actor with ActorLogging {

  import FileProcessor._

  override def receive = {

    case LoadFile =>
      log.info("Loading file: {}", file.getName)
      val lines = Source.fromFile(file).getLines()

      lines.drop(1).foreach(sensorDataStreamer ! SensorDataStreamer.ProcessLine(_))
      sensorDataStreamer ! FileProcessor.EndOfFile(file.getName)

    case FileProcessor.EndOfFile(file) =>
      log.info("File processed: {}", file)
      supervisor ! FileProcessor.FileLoaded(file)
  }

}
