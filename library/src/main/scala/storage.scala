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
      val favicon = dir.listFiles.find(_.getName == "favicon.ico").
        map { _.toURI }
      val propFiles = if (isDefaultLang) propFile(base).toSeq
                      else propFile(base).toSeq ++ propFile(dir).toSeq
      val layouts = dir.listFiles.filter(_.getName == "layouts").
        flatMap(_.listFiles.map { f => (f.getName, read(f))})
      lang -> Contents(lang, isDefaultLang, rootSection(dir, propFiles), css, files,
        favicon, defaultTemplate, layouts)
    }: _*)
    Globalized(contents, defaultTemplate)
  }
  def isSpecialDir(dir: File): Boolean =
    dir.isDirectory && ((dir.getName == "layouts") || (dir.getName == "files"))
  def rootSection(dir: File, propFiles: Seq[File]): Section = {
    def emptySection = Section("", "", Seq.empty, Nil, defaultTemplate)
    if (dir.exists) section("", dir, propFiles).headOption getOrElse emptySection
    else emptySection
  }
  def section(localPath: String, dir: File, propFiles: Seq[File]): Seq[Section] = {
    val files: List[File] = (Option(dir.listFiles) match {
      case None        => Nil
      case Some(fs) => fs.toList
    }).sortWith {
      _.getName < _.getName
    }
    files.find(isMarkdown).map { head =>
      val (raw, blocks, template) = knock(head, propFiles)
      val childFiles = files.filterNot { _ == head } filterNot { f =>
        f.isDirectory && defaultTemplate.languages.contains(f.getName)
      }
      val children = childFiles.flatMap { f =>
        if (isMarkdown(f))
          Seq(Leaf(localPath + "/" + f.getName, knock(f, propFiles)))
        else if (f.isDirectory && !isSpecialDir(f)) section(localPath + "/" + f.getName, f, propFiles)
        else Seq()
      }
      Section(localPath, raw, blocks, children, template)
    }.toSeq
  }
  def read(file: File) = doWith(scala.io.Source.fromFile(file)) { source =>
    source.mkString("")
  }
  def knock(file: File, propFiles: Seq[File]): (String, Seq[Block], Template) = 
    Knock.knockEither(read(file), propFiles) match {
      case Right(x) => x
      case Left(x) =>
        Console.err.println("Error while processing " + file.toString)
        throw x
    }
  def isMarkdown(f: File) = {
    !f.isDirectory &&
    !f.getName.startsWith(".") &&
    (f.getName.endsWith(".markdown") || f.getName.endsWith(".md"))
  }
  def defaultTemplate = StringTemplate(propFile(base).toSeq, None, Map())
  def doWith[T <: { def close() }, R](toClose: T)(f: T => R): R = {
    try {
      f(toClose)
    } finally {
      toClose.close()
    }
  }
}
