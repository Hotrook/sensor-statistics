package com.hotrook.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, Props}

object FileProcessor {
  def props: Props = Props(new FileProcessor)

  case class LoadFile(file: File)

  case class FileLoaded(files: String)

}

class FileProcessor extends Actor with ActorLogging {

  import FileProcessor._

  override def receive = {
    case LoadFile(file) =>
      log.info("Loading file: {}", file.getName)
      sender() ! FileLoaded(file.getName)
  }


}