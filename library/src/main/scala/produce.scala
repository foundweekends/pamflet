package pamflet

import java.io.{File,FileOutputStream}

object Produce {
  def apply(contents: Contents, target: File) {
    def write(path: String, contents: String) {
      val file = new File(target, path)
      new File(file.getParent).mkdirs()
      val out = new FileOutputStream(file)
      out.write(contents.getBytes("utf-8"))
      out.close()
    }
    val manifest = "pamflet.manifest"
    val printer = Printer(contents, Some(manifest))
    contents.pages.foreach { page =>
      write(Printer.fileify(page.name), printer.print(page).toString)
    }
    val css = contents.css.map { case (nm, v) => ("css/" + nm, v) }.toList
    css.foreach { case (path, contents) =>
      write(path, contents)
    }
    val paths = filePaths(contents)
    paths.foreach { path =>
      write(path, scala.io.Source.fromInputStream(
        new java.net.URL(Shared.resources, path).openStream()
      ).mkString(""))
    }
    write(manifest, (
      "CACHE MANIFEST" ::
      css.map { case (n,_) => n } :::
      contents.pages.map { p => Printer.webify(p.name) } :::
      paths).mkString("\n")
    )
  }
  def filePaths(contents: Contents) =
    "css/pamflet.css" :: "css/pamflet-grid.css" ::
    ("screen.css" :: "grid.css" :: "print.css" :: "ie.css" :: Nil).map {
      "css/blueprint/" + _
    } :::
    ("jquery-1.6.2.min.js" ::
     "pamflet.js" :: Nil
    ).map { "js/" + _ } :::
    "css/prettify.css" ::
    ("prettify.js" ::
      contents.langs.map { l => "lang-%s.js".format(l) }.toList
    ).map {
      "js/prettify/" + _
    }
}
