package pamflet

object App {
  def main(args: Array[String]) {
    Preview.run(new Contents(
      new FileStorage(new java.io.File("docs"))
    ))
  }
}
