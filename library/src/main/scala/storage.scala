package pamflet

import java.io.File

trait Storage {
  def contents: Contents
}

case class FileStorage(base: File, template: Template) extends Storage {
  def contents = {
    val pamflet = section(base).firstOption.getOrElse {
      Section("Empty", Seq.empty, Nil)
    }
    val css = base.listFiles.filter {
      _.getName.endsWith(".css")
    }.map { f => (f.getName, read(f)) }
    Contents(pamflet, css)
  }
  def section(dir: File): Seq[Section] = {
    val files = (dir.listFiles match {
      case null => Seq.empty
      case files => files
    }).toList.sort {
      _.getName < _.getName
    }
    files.find(isMarkdown).map { head =>
      val blocks = knock(head)
      val childFiles = files.filter { _ != head }
      val children = childFiles.flatMap { f =>
        if (isMarkdown(f))
          Seq(Leaf(knock(f)))
        else section(f)
      }
      Section(Page.name(blocks), blocks, children)
    }.toSeq
  }
  def read(file: File) = scala.io.Source.fromFile(file).mkString("")
  def knock(file: File) = 
    PamfletDiscounter.knockoff(template(read(file)))
  def isMarkdown(f: File) = (
    !f.isDirectory &&
    !f.getName.startsWith(".") &&
    (f.getName.endsWith(".markdown") || f.getName.endsWith(".md"))
  )
}
