package pamflet

class Pamflet extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    config.arguments match {
      case Array(Dir(input), Dir(output)) =>
        val template =
          StringTemplate(new java.io.File(input, "template.properties"))
        val storage = FileStorage(input, template)
        Produce(storage.contents, output)
        println("Wrote pamflet to " + output)
        Exit(0)
      case _ =>
        println("Usage: pf SRC DEST")
        Exit(1)
    }
  }
  object Dir {
    def unapply(path: String) = {
      val file = new java.io.File(path)
      if (file.exists && file.isDirectory)
        Some(file)
      else None
    }
  }
  case class Exit(val code: Int) extends xsbti.Exit
}
