package pamflet

import java.io.File
import com.tristanhunt.knockoff._

trait Storage {
  def contents: Contents
}

case class FileStorage(base: File, propfile: Option[File]) extends Storage {
  def contents = {
    val pamflet = section(base).headOption.getOrElse {
      Section(Seq.empty, Nil, defaultTemplate)
    }
    val css = base.listFiles.filter {
      _.getName.endsWith(".css")
    }.map { f => (f.getName, read(f)) }
    val files = base.listFiles.filter(_.getName=="files")
      .flatMap(_.listFiles.map { f => (f.getName, f.toURI) })
    val favicon = base.listFiles.filter(_.getName == "favicon.ico").headOption
      .map { _.toURI }
    Contents(pamflet, css, files, favicon, defaultTemplate)
  }
  def section(dir: File): Seq[Section] = {
    val files = (dir.listFiles match {
      case null => Array.empty
      case files => files
    }).toList.sortWith {
      _.getName < _.getName
    }
    files.find(isMarkdown).map { head =>
      val (blocks, template) = knock(head)
      val childFiles = files.filter { _ != head }
      val children = childFiles.flatMap { f =>
        if (isMarkdown(f))
          Seq(Leaf(knock(f)))
        else section(f)
      }
      Section(blocks, children, template)
    }.toSeq
  }
  def read(file: File) = scala.io.Source.fromFile(file).mkString("")
  def knock(file: File): (Seq[Block], Template) = { 
    val frontin = Frontin(read(file))
    val template = StringTemplate(propfile, frontin header)
    PamfletDiscounter.knockoff(template(frontin body)) -> template
  }
  def isMarkdown(f: File) = (
    !f.isDirectory &&
    !f.getName.startsWith(".") &&
    (f.getName.endsWith(".markdown") || f.getName.endsWith(".md"))
  )
  def defaultTemplate = StringTemplate(propfile, None)
}
