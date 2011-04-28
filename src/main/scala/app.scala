package pamflet

object App {
  def main(args: Array[String]) {
    new Contents(new FileStorage(new java.io.File("docs"))).contents.foreach {
      case Page(name,_) => println(name)
    }
  }
}
