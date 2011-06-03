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
    val printer = Printer(contents)
    contents.pages.foreach { page =>
      write(Printer.fileify(page.name), printer.print(page).toString)
    }
    contents.css.foreach { case (name, contents) =>
      write("css/" + name, contents)
    }
    filePaths(contents).foreach { path =>
      write(path, scala.io.Source.fromInputStream(
        new java.net.URL(Shared.resources, path).openStream()
      ).mkString(""))
    }
  }
  def filePaths(contents: Contents) =
    "css/pamflet.css" :: "css/pamflet-grid.css" ::
    ("screen.css" :: "grid.css" :: "print.css" :: "ie.css" :: Nil).map {
      "css/blueprint/" + _
    } :::
    "css/prettify.css" ::
    ("prettify.js" ::
      contents.langs.map { l => "lang-%s.js".format(l) }.toList
    ).map {
      "js/prettify/" + _
    }
}
