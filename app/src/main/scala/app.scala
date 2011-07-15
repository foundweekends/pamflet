package pamflet
import java.io.File

class Pamflet extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    config.arguments match {
      case Array(Dir(input), Dir(output)) =>
        val template =
          StringTemplate(new File(input, "template.properties"))
        val storage = FileStorage(input, template)
        Produce(storage.contents, output)
        println("Wrote pamflet to " + output)
        Exit(0)
      case Array(Dir(dir)) => preview(dir)
      case Array() =>
        "docs" match {
          case Dir(docs) => preview(docs)
          case _ =>
            println("""Usage: pf [SRC] [DEST]
                    |
                    |Default SRC is ./docs""".stripMargin)
            Exit(1)
        }
      case _ =>
        println("Input paths must be directories")
        Exit(1)
    }
  }
  def preview(dir: File) = {
    val properties = new File(dir, "template.properties")
    val storage = FileStorage(dir, StringTemplate(properties))
    Preview(storage.contents).run { server =>
      unfiltered.util.Browser.open(
        "http://127.0.0.1:%d/".format(server.port)
      )
      println("\nPreviewing `%s`. Press CTRL+C to stop.".format(dir))
    }
    Exit(0)
  }
  object Dir {
    def unapply(path: String) = {
      val file = new File(path)
      if (file.exists && file.isDirectory)
        Some(file)
      else None
    }
  }
  case class Exit(val code: Int) extends xsbti.Exit
}
