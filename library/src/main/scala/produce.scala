package pamflet

import java.io.{File,FileOutputStream,InputStream,
                OutputStream,ByteArrayInputStream,Reader,StringReader}

import scala.annotation.tailrec

object Produce {
  def apply(globalized: Globalized, target: File) {
    globalized.languages foreach { lang =>
      if (lang == globalized.defaultLanguage) apply(globalized.defaultContents, globalized, target)
      else apply(globalized(lang), globalized, new File(target, lang))
    }
  }
  def apply(contents: Contents, globalized: Globalized, target: File) {
    def writeString(path: String, contents: String, target:File) {
      write(path, target, new ByteArrayInputStream(contents.getBytes("utf-8")))
    }
    def write(path: String, target:File, r: InputStream) {
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
    val offlineTarget = new File(target + "/offline/")
    val css = contents.css.map { case (nm, v) => ("css/" + nm, v) }.toList
    val paths = filePaths(contents)
    val files = contents.files.toList.map {
      case (nm, u) => ("files/" + nm, u)
    }
    val favicon = contents.favicon.toList.map {
      case u => ("favicon.ico", u)
    }

    // generate the pages in target directory and in 
    // subdirectory "offline" with html5 manifest 
    List(Some(manifest), None).foreach { manifestOpt =>
      val offline = ! manifestOpt.isEmpty
      val targetDir = (if (offline) offlineTarget else target)
      val printer = Printer(contents, globalized, manifestOpt)
      contents.pages.foreach { page => 
        val w = new java.io.StringWriter()
        xml.XML.write(w, 
                      printer.print(page),
                      "utf-8",
                      xmlDecl = false,
                      doctype = xml.dtd.DocType(
                        "html",
                        xml.dtd.SystemID("about:legacy-compat"),
                        Nil
                      )
        )
        val pagePath = Printer.fileify(page) 
        writeString(pagePath, w.toString, targetDir)
      } 
      css.foreach { case (path, contents) =>
        writeString(path, contents, targetDir) 
      }

      paths.foreach { path =>
        write(path,
          targetDir,
          new java.net.URL(Shared.resources, path).openStream()
        )
      }

      for ((path, uri) <- files ++ favicon)
        write(path, targetDir, uri.toURL.openStream)
    }

    writeString(manifest, (
      "CACHE MANIFEST" ::
      // cache file must change between updates
      ("# " + new java.util.Date) ::
      css.map { case (n,_) => n } :::
      contents.pages.map { p => Printer.webify(p) } :::
      files.map { case (n, _) => n } :::
      favicon.map { case (n, _) => n } :::
      paths).mkString("\n"),
      offlineTarget
    )
  }
  def filePaths(contents: Contents) =
    ("fork.png" :: "twitter-bird-dark-bgs.png" :: Nil).map {
      "img/" + _
    } :::
    ("pamflet.css" :: "pamflet-grid.css" :: "pamflet-print.css" ::
     "color_scheme-redmond.css" :: "color_scheme-github.css" :: "color_scheme-monokai.css" :: Nil).map {
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
      contents.prettifyLangs.map { l => "lang-%s.js".format(l) }.toList
    ).map {
      "js/prettify/" + _
    }
}
