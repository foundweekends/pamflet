package pamflet

object Heights {
  def heightCssFileName(page: Page): String =
    heightCssFileName(headerHeight(page), footerHeight(page))
  def heightCssFileName(hh: String, fh: String): String =
    s"pamfletheight_${hh}_${fh}".replaceAll("\\W", "_") + ".css"
  def headerHeight(page: Page): String =
    page.template.get("layout.header.height").getOrElse("2em")
  def footerHeight(page: Page): String =
    page.template.get("layout.footer.height").getOrElse("2em")
  def heightCssFileContent(hh: String, fh: String): String =
    s"""@charset "utf-8";
    |a.page {
    |  top: $hh;
    |  bottom: $fh;
    |}
    |div.header {
    |  height: $hh;
    |}
    |div.footer {
    |  height: $fh
    |}
    |div.contentswrapper {
    |  padding-top: $hh;
    |  padding-bottom: $fh;
    |}
    |""".stripMargin
  def heightCssFileContent(contents: Contents, fileName: String): String =
    (distinctHeights(contents) find { case (hh, fh) =>
      heightCssFileName(hh, fh) == fileName
    } map { case (hh, fh) =>
      heightCssFileContent(hh, fh)
    }).getOrElse {
      sys.error("$fileName was not found")
    }

  def distinctHeights(contents: Contents): List[(String, String)] =
    (contents.pages map { page =>
      (headerHeight(page), footerHeight(page))
    }).toList.distinct
}
