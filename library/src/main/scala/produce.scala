package pamflet

import java.io.{File,FileOutputStream,InputStream,
                OutputStream,ByteArrayInputStream,Reader,StringReader}

import scala.annotation.tailrec

object Produce {
  def apply(contents: Contents, target: File) {
    def writeString(path: String, contents: String) {
      write(path, new ByteArrayInputStream(contents.getBytes("utf-8")))
    }
    def write(path: String, r: InputStream) {
      val file = new File(target, path)
      new File(file.getParent).mkdirs()
      val w = new FileOutputStream(file)
      copy(r, w)
      r.close()
      w.close()
    }
    def copy(r: InputStream, w: OutputStream) {
      @tailrec def doCopy: Unit = {
        val byte = r.read()
        if (byte != -1) {
          w.write(byte)
          doCopy
        }
      }
      doCopy
      w.flush()
    }
    val manifest = "pamflet.manifest"
    val printer = Printer(contents, Some(manifest))
    contents.pages.foreach { page =>
      writeString(Printer.fileify(page), printer.print(page).toString)
    }
    val css = contents.css.map { case (nm, v) => ("css/" + nm, v) }.toList
    css.foreach { case (path, contents) =>
      writeString(path, contents)
    }
    val paths = filePaths(contents)
    paths.foreach { path =>
      write(path,
        new java.net.URL(Shared.resources, path).openStream()
      )
    }
    writeString(manifest, (
      "CACHE MANIFEST" ::
      // cache file must change between updates
      ("# " + new java.util.Date) ::
      css.map { case (n,_) => n } :::
      contents.pages.map { p => Printer.webify(p) } :::
      paths).mkString("\n")
    )
  }
  def filePaths(contents: Contents) =
    "img/fork.png" ::
    ("pamflet.css" :: "pamflet-grid.css" :: "pamflet-print.css" :: Nil).map {
      "css/" + _
    } :::
    ("screen.css" :: "grid.css" :: "print.css" :: "ie.css" :: Nil).map {
      "css/blueprint/" + _
    } :::
    ("jquery-1.6.2.min.js" ::
     "jquery.collapse.js" ::
     "pamflet.js" :: Nil
    ).map { "js/" + _ } :::
    "css/prettify.css" ::
    ("prettify.js" ::
      contents.langs.map { l => "lang-%s.js".format(l) }.toList
    ).map {
      "js/prettify/" + _
    }
}
