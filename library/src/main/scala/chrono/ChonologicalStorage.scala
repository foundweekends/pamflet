package pamflet.chrono

import java.io.File

import pamflet.{FileStorage,Section}

case class ChronologicalStorage(base: File) extends FileStorage {
  import FileStorage._
  def rootSection(dir: File, propFiles: Seq[File]): Section = ???
}
