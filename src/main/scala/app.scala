package pamflet

object App {
  def main(args: Array[String]) {
    new FileStorage(new java.io.File("docs")).items.foreach(println)
  }
}
