package com.hotrook.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.io.Source

object FileProcessor {
  def props(supervisor: ActorRef, sensorDataStreamer: ActorRef): Props = Props(new FileProcessor(
    supervisor,
    sensorDataStreamer
  ))

  case class LoadFile(file: File)

  case class FileLoaded(files: String)

  case class EndOfFile(filename: String)

}

class FileProcessor(supervisor: ActorRef, sensorDataStreamer: ActorRef) extends Actor with ActorLogging {

  import FileProcessor._


  override def receive = {
    case LoadFile(file) =>
      log.info("Loading file: {}", file.getName)
      val lines = Source.fromFile(file).getLines()
      removeHeader(lines)
      lines.foreach {
        sensorDataStreamer ! SensorDataStreamer.ProcessLine(_)
      }
      sensorDataStreamer ! FileProcessor.EndOfFile(file.getName)
    case FileProcessor.EndOfFile(file) =>
      log.info("File processed: {}", file)
      supervisor ! FileProcessor.FileLoaded(file)
  }

  private def removeHeader(lines: Iterator[String]) = {
    if (lines.hasNext)
      lines.next()
  }

}
