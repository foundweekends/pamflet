package pamflet

import sbt._
import Keys._
import Defaults._


object Pamflet extends Plugin {
  val pamfletDocs = SettingKey[File]("pamflet-docs")
  val pamfletProperties = SettingKey[File]("pamflet-properties")
  val pamfletOutput = SettingKey[File]("pamflet-output")
  val pamfletStorage = SettingKey[Storage]("pamflet-storage")
  val pamfletServer = SettingKey[unfiltered.jetty.Http]("pamflet-server")
  val startPamflet = TaskKey[Unit]("start-pamflet")
  val stopPamflet = TaskKey[Unit]("stop-pamflet")
  val writePamflet = TaskKey[Unit]("write-pamflet")

  override lazy val settings = 
    baseSettings ++ Seq(
      pamfletDocs, pamfletProperties, pamfletOutput, pamfletStorage,
      pamfletServer, startPamflet, stopPamflet, writePamflet
    ).map { s => (aggregate in s) := false }

  val baseSettings: Seq[Project.Setting[_]] = Seq(
    pamfletDocs <<= baseDirectory / "docs",
    pamfletProperties <<= pamfletDocs / "template.properties",
    pamfletOutput <<= target / "docs",
    pamfletStorage <<= (pamfletDocs, pamfletProperties) {
      (docs, properties) =>
        FileStorage(docs, StringTemplate(properties))
    },
    pamfletServer <<= (pamfletStorage) { (storage) =>
      Preview(storage.contents)
    },
    startPamflet <<= startPamfletTask,
    stopPamflet <<= stopPamfletTask,
    writePamflet <<= writePamfletTask
  )

  private def startPamfletTask = (pamfletServer) map { (server) =>
    server.start
    unfiltered.util.Browser.open(
      "http://127.0.0.1:%d/".format(server.port)
    )
    ()
  }
  private def stopPamfletTask = (pamfletServer) map { (server) =>
    server.stop
    ()
  }
  private def writePamfletTask = (pamfletOutput, pamfletStorage) map {
    (output, storage) =>
      sbt.IO.createDirectory(output)
      Produce(storage.contents, output)
  }

}
