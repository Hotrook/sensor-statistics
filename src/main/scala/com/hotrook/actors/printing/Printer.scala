package com.hotrook.actors.printing

import akka.actor.{Actor, ActorLogging, Props}
import com.hotrook.actors.ResultsCollector

object Printer {
  def props: Props = Props(new Printer)

  case class Print(text : String)
}

class Printer extends Actor with ActorLogging {
  override def receive : Receive = {
    case Printer.Print(text) => println(text)
    case ResultsCollector.EndOfData => context stop self
  }
}
