package pamflet

trait Actions extends sbt.Project {
  def pamfletDocs = path("docs")
  def pamfletProperties = pamfletDocs / "template.properties"
  def pamfletOutput = outputPath / "docs"
  private def pamfletStorage = 
    FileStorage(pamfletDocs.asFile, 
                StringTemplate(pamfletProperties.asFile))
  lazy private val pamfletServer = Preview(pamfletStorage.contents)
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
      Produce(
        pamfletStorage.contents, pamfletOutput.asFile
      )
      None
    }
  }
}
