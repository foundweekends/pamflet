package pamflet
import java.io.File

class Pamflet extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    Exit(Pamflet.run(config.arguments))
  }
  case class Exit(val code: Int) extends xsbti.Exit
}

object Pamflet {
  def main(args: Array[String]) {
    System.exit(run(args))
  }
  private def storage(dir: File) = FileStorage(dir)
  def run(args: Array[String]) = {
    args match {
      case Array(Dir(input), Dir(output)) =>
        Produce(storage(input).globalized, output)
        println("Wrote pamflet to " + output)
        0
      case Array(Dir(dir)) => preview(dir)
      case Array() =>
        "docs" match {
          case Dir(docs) => preview(docs)
          case _ =>
            println("""Usage: pf [SRC] [DEST]
                    |
                    |Default SRC is ./docs""".stripMargin)
            1
        }
      case _ =>
        println("Input paths must be directories")
        1
    }
  }
  def preview(dir: File) = {
    Preview(storage(dir).globalized).run { server =>
      unfiltered.util.Browser.open(
        "http://127.0.0.1:%d/".format(server.port)
      )
      println("\nPreviewing `%s`. Press CTRL+C to stop.".format(dir))
    }
    0
  }
  object Dir {
    def unapply(path: String) = {
      val file = new File(path)
      if (file.exists && file.isDirectory)
        Some(file)
      else None
    }
  }
}
