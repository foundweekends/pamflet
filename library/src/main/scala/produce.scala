package pamflet

import java.io.{File,FileOutputStream,InputStreamReader,OutputStream,Reader,
                StringReader}

import scala.annotation.tailrec

object Produce {
  def apply(contents: Contents, target: File) {
    def writeString(path: String, contents: String) {
      write(path, new StringReader(contents))
    }
    def write(path: String, r: Reader) {
      val file = new File(target, path)
      new File(file.getParent).mkdirs()
      val os = new FileOutputStream(file)
      copy(r, os)
      r.close()
      os.close()
    }
    def copy(r: Reader, os: OutputStream) {
      @tailrec def doCopy: Unit = {
        val byte = r.read()
        if (byte != -1) {
          os.write(byte)
          doCopy
        }
      }
      doCopy
      os.flush()
    }
    val manifest = "pamflet.manifest"
    val printer = Printer(contents, Some(manifest))
    contents.pages.foreach { page =>
      writeString(Printer.fileify(page.name), printer.print(page).toString)
    }
    val css = contents.css.map { case (nm, v) => ("css/" + nm, v) }.toList
    css.foreach { case (path, contents) =>
      writeString(path, contents)
    }
    val paths = filePaths(contents)
    paths.foreach { path =>
      write(path, new InputStreamReader(
        new java.net.URL(Shared.resources, path).openStream()
      ))
println("*** ... written.")
    }
    writeString(manifest, (
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
