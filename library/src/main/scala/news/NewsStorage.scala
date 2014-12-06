package pamflet.chrono

import java.io.File

import pamflet.{FileStorage,ChronologicalIndex}

case class ChronologicalStorage(base: File) extends FileStorage {
  import FileStorage._
  def frontPage(dir: File, propFiles: Seq[File]): ChronologicalIndex = ???
  val children = Nil
}
