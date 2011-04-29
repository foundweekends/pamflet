package pamflet

object App {
  def main(args: Array[String]) {
    Produce(new Contents(
      new FileStorage(new java.io.File("docs"))
    ), new java.io.File("target"))
  }
}
