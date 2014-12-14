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
  def run(args: Array[String]) = {
    args match {
      case Array(Dir(input), Dir(output)) =>
        Produce(StructuredFileStorage(input).globalContents, output)
        println("Wrote pamflet to " + output)
        0
      case Array(nm @ Dir(dir)) if nm == "news" =>
        preview(dir, news.NewsStorage)
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
  def preview(dir: File, collation: Collation = StructuredFileStorage) = {
    Preview(collation(dir).globalContents).run { server =>
      unfiltered.util.Browser.open(
        server.portBindings.head.url
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
  type Collation = (File => FileStorage)
}
