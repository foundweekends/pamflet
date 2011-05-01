package pamflet

trait Storage {
  def items: Seq[CharSequence]
  def css: Seq[(String,String)]
}

trait UriStorage extends Storage {
  def uris: Seq[java.net.URI]
  def read(uri: java.net.URI) =
      scala.io.Source.fromInputStream(uri.toURL.openStream).mkString("")
  def items = uris.map(read)
}

case class FileStorage(base: java.io.File) extends UriStorage {
  def uris = base.listFiles.filter {
    _.getName.endsWith(".markdown")
  }.toList.sort { _.getName < _.getName } map { _.toURI }
  def css = base.listFiles.filter {
    _.getName.endsWith(".css")
  }.map { f => (f.getName, read(f.toURI)) }
}
