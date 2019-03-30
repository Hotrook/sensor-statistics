package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps


class DirectoryScannerSpec(_system: ActorSystem) extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("DirectoryScannerSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "DirectoryScanner should" should {

    "send message with 0 files found" in {
      val supervisor = TestProbe()
      val directoryScanner = system.actorOf(DirectoryScanner.props(supervisor.ref))

      supervisor.send(directoryScanner, DirectoryScanner.ScanDirectory("src/test/resources/testDirectories/empty-dixcr"))

      supervisor.expectMsg(DirectoryScanner.FilesFound(0))
    }

    "create 3 FileProcessors" in {
      val supervisor = TestProbe()
      val directoryScanner = system.actorOf(DirectoryScanner.props(supervisor.ref))

      supervisor.send(directoryScanner, DirectoryScanner.ScanDirectory("src/test/resources/testDirectories/3-files-dir"))

      supervisor.expectMsg(DirectoryScanner.FilesFound(3))

      supervisor.expectMsgAllOf(
        FileProcessor.FileLoaded("file1.csv"),
        FileProcessor.FileLoaded("file2.csv"),
        FileProcessor.FileLoaded("file3.csv"))
    }
  }
}
