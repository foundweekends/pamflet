package pamflet

import java.io.File
import com.tristanhunt.knockoff._
import collection.immutable.Map

trait Storage {
  def globalized: Globalized
}

case class FileStorage(base: File) extends Storage {
  def propFile(dir: File): Option[File] =
    new File(dir, "template.properties") match {
      case file if file.exists => Some(file)
      case _                   => None
    }
  def globalized = {
    val contents = Map(defaultTemplate.languages map { lang =>
      val isDefaultLang = lang == defaultTemplate.defaultLanguage
      val dir = if (isDefaultLang) base
                else new File(base, lang)
      val css = dir.listFiles.filter {
        _.getName.endsWith(".css")
      }.map { f => (f.getName, read(f)) }
      val files = dir.listFiles.filter(_.getName=="files").
        flatMap(_.listFiles.map { f => (f.getName, f.toURI) })
      val favicon = dir.listFiles.filter(_.getName == "favicon.ico").headOption.
        map { _.toURI }
      val propFiles = if (isDefaultLang) propFile(base).toSeq
                      else propFile(base).toSeq ++ propFile(dir).toSeq
      lang -> Contents(lang, isDefaultLang, rootSection(dir, propFiles), css, files, favicon, defaultTemplate)
    }: _*)
    Globalized(contents, defaultTemplate)
  }
  def rootSection(dir: File, propFiles: Seq[File]): Section = {
    def emptySection = Section("", Seq.empty, Nil, defaultTemplate)
    if (dir.exists) section("", dir, propFiles).headOption getOrElse emptySection
    else emptySection
  }
  def section(localPath: String, dir: File, propFiles: Seq[File]): Seq[Section] = {
    val files: List[File] = (Option(dir.listFiles) match {
      case None        => Nil
      case Some(files) => files.toList
    }).sortWith {
      _.getName < _.getName
    }
    files.find(isMarkdown).map { head =>
      val (blocks, template) = knock(head, propFiles)
      val childFiles = files.filterNot { _ == head } filterNot { f =>
        f.isDirectory && defaultTemplate.languages.contains(f.getName)
      }
      val children = childFiles.flatMap { f =>
        if (isMarkdown(f))
          Seq(Leaf(localPath + "/" + f.getName, knock(f, propFiles)))
        else section(localPath + "/" + f.getName, f, propFiles)
      }
      Section(localPath, blocks, children, template)
    }.toSeq
  }
  def read(file: File) = scala.io.Source.fromFile(file).mkString("")
  def knock(file: File, propFiles: Seq[File]): (Seq[Block], Template) = { 
    val frontin = Frontin(read(file))
    val template = StringTemplate(propFiles, frontin header)
    try {
      PamfletDiscounter.knockoff(template(frontin body)) -> template
    } catch {
      case e: Throwable =>
        Console.err.println("Error while processing " + file.toString)
        throw e
    }
  }
  def isMarkdown(f: File) = (
    !f.isDirectory &&
    !f.getName.startsWith(".") &&
    (f.getName.endsWith(".markdown") || f.getName.endsWith(".md"))
  )
  def defaultTemplate = StringTemplate(propFile(base).toSeq, None)
}
