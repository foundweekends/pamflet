package pamflet

class Pamflet extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    config.arguments match {
      case Array(Dir(input), Dir(output)) =>
        Produce(new Contents(new FileStorage(input)), output)
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
