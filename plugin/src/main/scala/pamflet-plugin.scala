package pamflet

trait Actions extends sbt.Project {
  def pamfletDocs = path("docs")
  def pamfletOutput = outputPath / "docs"
  lazy val previewPamflet = task {
    Preview(new Contents(new FileStorage(pamfletDocs.asFile)))
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
