package pamflet

import java.io.File
import com.tristanhunt.knockoff._
import collection.immutable.Map
import java.nio.charset.Charset

trait Storage {
  def globalContents: GlobalContents
}

trait FileStorage extends Storage {
  import FileStorage._
  def base: File
  def frontPage(dir: File, propFiles: Seq[File], contentParents: List[String]): Page
  def template = StringTemplate(propFile(base).toSeq, None, Map())
  def globalContents = {
    def css(dir: File) = dir.listFiles.filter {
      _.getName.endsWith(".css")
    }.map { f => (f.getName, read(f)) }
    def files(dir: File) = dir.listFiles.filter(_.getName=="files").
      flatMap(_.listFiles.map { f => (f.getName, f.toURI) })
    def favicon(dir: File) = dir.listFiles.find(_.getName == "favicon.ico").
      map { _.toURI }
    def layouts(dir: File) = dir.listFiles.filter(_.getName == "layouts").
      flatMap(_.listFiles.map { f => (f.getName, read(f))})

    val basePropFile = propFile(base)
    val baseContents = Contents(
      template.defaultLanguage,
      true,
      frontPage(base, basePropFile.toSeq, Nil),
      css(base),
      files(base),
      favicon(base),
      layouts(base)
    )
    val contentsByLanguage =
      for (lang <- template.languages if lang != template.defaultLanguage) yield {
        val dir = new File(base, lang)
        lang -> Contents(
          lang,
          false,
          frontPage(dir, propFile(dir).toSeq ++ basePropFile, lang :: Nil),
          css(dir) ++ baseContents.css,
          files(dir) ++ baseContents.files,
          favicon(dir) orElse baseContents.favicon,
          layouts(dir) ++ baseContents.layouts
        )
      }
    GlobalContents(
      contentsByLanguage.toMap + (template.defaultLanguage -> baseContents),
      template
    )
  }
  def knock(file: File, propFiles: Seq[File]): (String, Seq[Block], Template) = 
    Knock.knockEither(read(file, template.defaultEncoding), propFiles) match {
      case Right(x) => x
      case Left(x) =>
        Console.err.println("Error while processing " + file.toString)
        throw x
    }
}
case class StructuredFileStorage(base: File) extends FileStorage {
  import FileStorage._
  def frontPage(dir: File, propFiles: Seq[File], contentParents: List[String]): Section = {
    def emptySection = Section("", "", Seq.empty, Nil, template, contentParents)
    val entered = section("", dir, propFiles, contentParents).headOption.getOrElse(
      emptySection
    )
    Section(
      entered.localPath,
      entered.raw,
      entered.blocks,
      entered.children :::
        DeepContents(entered.template, contentParents) ::
        ScrollPage(entered, entered.template, contentParents) ::
        Nil,
      entered.template,
      contentParents
    )

  }
  def section(localPath: String, dir: File, propFiles: Seq[File], contentParents: List[String]): Seq[Section] = {
    val files: List[File] = (Option(dir.listFiles) match {
      case None        => Nil
      case Some(fs) => fs.toList
    }).sortWith {
      _.getName < _.getName
    }
    files.find(isMarkdown).map { head =>
      val (raw, blocks, template) = knock(head, propFiles)
      val childFiles = files.filterNot { _ == head } filterNot { f =>
        f.isDirectory && template.languages.contains(f.getName)
      }
      val children = childFiles.flatMap { f =>
        if (isMarkdown(f))
          Seq(Leaf(localPath + "/" + f.getName, knock(f, propFiles), contentParents))
        else if (f.isDirectory && !isSpecialDir(f)) section(localPath + "/" + f.getName, f, propFiles, contentParents)
        else Seq()
      }
      Section(localPath, raw, blocks, children, template, contentParents)
    }.toSeq
  }
}
object FileStorage {
  def propFile(dir: File): Option[File] =
    Some(new File(dir, "template.properties")).filter(_.exists)
  def isSpecialDir(dir: File): Boolean =
    dir.isDirectory && ((dir.getName == "layouts") || (dir.getName == "files"))
  def read(file: File, encoding: String = Charset.defaultCharset.name) = doWith(scala.io.Source.fromFile(file, encoding)) { source =>
    source.mkString("")
  }
  def isMarkdown(f: File) = {
    !f.isDirectory &&
    !f.getName.startsWith(".") &&
    (f.getName.endsWith(".markdown") || f.getName.endsWith(".md"))
  }
  def doWith[T <: { def close() }, R](toClose: T)(f: T => R): R = {
    try {
      f(toClose)
    } finally {
      toClose.close()
    }
  }
  def depthFirstFiles(parent: File, stack: List[File] = Nil): Stream[File] = {
    if (parent.isFile) {
      stack.headOption.fold(parent #:: Stream.empty)(
        h => parent #:: depthFirstFiles(h, stack.tail)
      )
    } else {
      val children = parent.listFiles.sorted(
        Ordering.by((_: File).getName).reverse
      ).toList
      depthFirstFiles(children.head, children.tail ::: stack)
    }
  }
  def parents(file: File): Stream[File] =
    Option(file.getParentFile).fold(Stream.empty[File])(
      p => p #:: parents(p)
    )
}
