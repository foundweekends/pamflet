package pamflet

import java.io.{File,FileOutputStream}

object Produce {
  def apply(contents: Contents, target: java.io.File) {
    val pages = contents.pages.toList
    val printer = new Printer(pages)
    pages.foreach { page =>
      val out = new FileOutputStream(new File(target, Printer.webify(page.name)))
      out.write(printer.print(page).toString.getBytes("utf-8"))
      out.close()
    }
  }
}
