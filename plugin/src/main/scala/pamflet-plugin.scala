package pamflet

trait Actions extends sbt.Project {
  def pamfletDocs = path("docs")
  def pamfletOutput = outputPath / "docs"
  lazy private val pamfletServer = Preview(new Contents(new FileStorage(pamfletDocs.asFile)))
  lazy val startPamflet = task {
    pamfletServer.start
    unfiltered.util.Browser.open("http://127.0.0.1:%d/".format(pamfletServer.port))
    None
  }
  lazy val stopPamflet = task {
    pamfletServer.stop
    None
  }
  lazy val writePamflet = task {
    sbt.FileUtilities.createDirectory(pamfletOutput, log) orElse {
      Produce(new Contents(
        new FileStorage(new java.io.File("docs"))
      ), pamfletOutput.asFile)
      None
    }
  }
}
