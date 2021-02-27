package pamflet

import java.io.File
import java.nio.charset.Charset
import knockoff._
import collection.immutable.Map
import collection.concurrent.TrieMap

trait Storage {
  def globalized: Globalized
}

/** Cache FileStorage based on the last modified time.
 * This should make previewing much faster on large pamflets.
 */
case class CachedFileStorage(base: File, ps: List[FencePlugin]) extends Storage {
  def allFiles(f0: File): collection.Seq[File] =
    f0.listFiles.toVector flatMap {
      case dir if dir.isDirectory => allFiles(dir)
      case f => Vector(f)
    }
  def maxLastModified(f0: File): Long =
    (allFiles(f0) map {_.lastModified}).max

  def globalized = {
    val lm = maxLastModified(base)
    CachedFileStorage.cache.get(base) match {
      case Some((lm0, gl0)) if lm == lm0 => gl0
      case _ =>
        val st = FileStorage(base, ps)
        val gl = st.globalized
        CachedFileStorage.cache(base) = (lm, gl)
        gl
    }
  }
}

object CachedFileStorage {
  val cache: TrieMap[File, (Long, Globalized)] = TrieMap()
}

case class FileStorage(base: File, ps: List[FencePlugin]) extends Storage {
  def propFile(dir: File): Option[File] =
    new File(dir, "template.properties") match {
      case file if file.exists => Some(file)
      case _                   => None
    }
  def globalized = {
    val contents = defaultTemplate.languages.map { lang =>
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
    }.toMap
    Globalized(contents, defaultTemplate)
  }
  def isSpecialDir(dir: File): Boolean =
    dir.isDirectory && ((dir.getName == "layouts") || (dir.getName == "files"))
  def rootSection(dir: File, propFiles: collection.Seq[File]): Section = {
    Knock.notifyBeginLanguage()
    def emptySection = Section("", "", Seq.empty, Nil, defaultTemplate)
    if (dir.exists) section("", dir, propFiles).headOption getOrElse emptySection
    else emptySection
  }
  def section(localPath: String, dir: File, propFiles: collection.Seq[File]): collection.Seq[Section] = {
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
        val childLocalPath = if (localPath.isEmpty) f.getName else localPath + "/" + f.getName
        if (isMarkdown(f)) Seq(Leaf(childLocalPath, knock(f, propFiles)))
        else if (f.isDirectory && !isSpecialDir(f)) section(childLocalPath, f, propFiles)
        else Seq()
      }
      Section(head.getName, raw, blocks, children, template)
    }.toSeq
  }
  def read(file: File, encoding: String = Charset.defaultCharset.name) = doWith(scala.io.Source.fromFile(file, encoding)) { source =>
    source.mkString("")
  }
  def knock(file: File, propFiles: collection.Seq[File]): (String, collection.Seq[Block], Template) = 
    Knock.knockEither(read(file, defaultTemplate.defaultEncoding), propFiles, ps) match {
      case Right(x) => x
      case Left(x) =>
        Console.err.println("Error while processing " + file.toString)
        // x.printStackTrace()
        throw x
    }
  def isMarkdown(f: File) = {
    !f.isDirectory &&
    !f.getName.startsWith(".") &&
    (f.getName.endsWith(".markdown") || f.getName.endsWith(".md"))
  }
  def defaultTemplate = StringTemplate(propFile(base).toSeq, None, Map())
  def doWith[T <: { def close(): Unit }, R](toClose: T)(f: T => R): R = {
    try {
      f(toClose)
    } finally {
      toClose.close()
    }
  }
}
