package pamflet

object App {
  def main(args: Array[String]) {
    val contents = new Contents(
      new FileStorage(new java.io.File("docs"))
    ).contents.toList
    val printer = new Printer(contents)
    contents.foreach { page =>
      print(printer.print(page))
    }
  }
}
