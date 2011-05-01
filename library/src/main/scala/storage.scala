package pamflet

trait Storage {
  def items: Seq[CharSequence]
}

trait UriStorage extends Storage {
  def uris: Seq[java.net.URI]
  def items =
    uris.map { uri =>
      scala.io.Source.fromInputStream(uri.toURL.openStream).mkString("")
    }
}

case class FileStorage(base: java.io.File) extends UriStorage {
  def uris = base.listFiles.filter {
    _.getName.endsWith(".markdown")
  }.toList.sort { _.getName < _.getName } map { _.toURI }
}
