package pamflet

trait Storage {
  def items: Seq[CharSequence]
}

trait UriStorage extends Storage {
  def uris: Seq[java.net.URI]
  def items =
    uris.view.map { uri =>
      scala.io.Source.fromURI(uri).mkString("")
    }
}

class FileStorage(base: java.io.File) extends UriStorage {
  def uris = base.listFiles.sortBy { _.getName } map { _.toURI }
}
