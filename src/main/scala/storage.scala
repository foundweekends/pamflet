package pamflet

trait Storage {
  def items: Seq[CharSequence]
}

trait UriStorage extends Storage {
  def uris: Seq[java.net.URL]
  def items =
    uris.map { uri =>
      scala.io.Source.fromInputStream(uri.openStream).mkString("")
    }
}

class FileStorage(base: java.io.File) extends UriStorage {
  def uris = base.listFiles.filter {
    _.getName.endsWith(".markdown")
  }.toList.sort { _.getName < _.getName } map { _.toURL }
}
