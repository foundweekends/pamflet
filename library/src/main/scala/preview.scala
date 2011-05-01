package pamflet

import unfiltered.request._
import unfiltered.response._

object Preview {
  def apply(contents: => Contents) = {
    def css = Map.empty ++ contents.css
    unfiltered.jetty.Http.anylocal.filter(unfiltered.filter.Planify {
      case GET(Path(Seg(Nil))) =>
        contents.pages.firstOption.map { page =>
          Redirect("/" + Printer.webify(page.name))
        }.getOrElse { NotFound }
      case GET(Path(Seg(name :: Nil))) =>
        Printer(contents).printNamed(name).map { html =>
          Html(html)
        }.getOrElse { NotFound }
      case GET(Path(Seg("css" :: name :: Nil))) if css.contains(name) =>
        CssContent ~> ResponseString(css(name))
    }).resources(Shared.resources)
  }
}
