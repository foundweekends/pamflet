package pamflet

import unfiltered.request._
import unfiltered.response._
import java.io.OutputStream
import java.net.URI
import collection.mutable

object Preview {
  val heightCache: mutable.Map[(String, String), String] = mutable.Map()

  def apply(globalContents: => GlobalContents) = {
    def languages = globalContents.template.languages

    def langContents(lang: String) = globalContents.byLanguage(lang)
    def defaultLanguage = globalContents.template.defaultLanguage

    object PagePath {
      def unapply(req: HttpRequest[_]) = {
        val Path(Seg(segs)) = req
        val (lang, pageSegs) =
          (for (seg <- segs.headOption if languages.contains(seg))
          yield (seg -> segs.tail)).getOrElse(
            (defaultLanguage, segs)
          )
        val pagePath = pageSegs.mkString("/")
        langContents(lang).pages.find(
          p => Printer.webify(p) == pagePath
        ).map((lang, _))
      }
    }

    def contentsDispatch(contents: Contents, segs: List[String]) = {
      segs match {
        case Nil =>
          contents.pages.headOption.map { page =>
            Redirect("/" + contents.pathOf(page))
          }.getOrElse { Pass }
        case "favicon.ico" :: Nil =>
            contents.favicon.map(responseStreamer).getOrElse(Pass)
        case "css" :: name :: Nil =>
          val css: Option[String] =
            if (name.startsWith("pamfletheight"))
              Some(heightCache.getOrElseUpdate(
                (contents.language, name),
                Heights.heightCssFileContent(contents, name)
              ))
            else contents.css.find(_._1 == name).map(_._2)
          css.map(str => CssContent ~> ResponseString(str)).getOrElse(Pass)
        case "files" :: name :: Nil =>
          contents.files.find(_._1 == name).map {
            case (_, url) => responseStreamer(url)
          } getOrElse Pass
        case _ => Pass
      }
    }

    unfiltered.netty.Server.anylocal.plan(unfiltered.netty.cycle.Planify {
      case GET(PagePath(lang, page)) =>
          Html5(Printer(langContents(lang), globalContents, None).print(page))
      case GET(Path(Seg(lang :: rest))) if languages.contains(lang) =>
        contentsDispatch(langContents(lang), rest)
      case GET(Path(Seg(segs))) =>
        contentsDispatch(langContents(defaultLanguage), segs)
    }).resources(Shared.resources)
  }
  def responseStreamer(uri: URI) =
    new ResponseStreamer { def stream(os:OutputStream) { 
      val is = uri.toURL.openStream
      try {
        val buf = new Array[Byte](1024)
        Iterator.continually(is.read(buf)).takeWhile(_ != -1)
          .foreach(os.write(buf, 0, _))
      } finally {
        is.close
      }
    } }
}
