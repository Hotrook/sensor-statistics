package com.hotrook.actors

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps


class DirectoryScannerSpec() extends TestKit(ActorSystem("DirectoryScannerSpec"))
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
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
