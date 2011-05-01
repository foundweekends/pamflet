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
      write(Printer.webify(page.name), printer.print(page).toString)
    }
    filePaths.foreach { path =>
      write(path, scala.io.Source.fromInputStream(
        new java.net.URL(Shared.resources, path).openStream()
      ).mkString(""))
    }
  }
  val filePaths =
    "css/pamflet.css" ::
    ("screen.css" :: "print.css" :: "ie.css" :: Nil).map { name =>
      "css/blueprint/%s".format(name)
    }
}
