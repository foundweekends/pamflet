package pamflet
import com.tristanhunt.knockoff.DefaultDiscounter.toXHTML

object Printer {
  def webify(name: String) =
    java.net.URLEncoder.encode(name, "utf-8") + ".html"
}
case class Printer(contents: Contents) {
  def toc(current: Page) =
    <ol> { contents.pages.map {
      case `current` => <li>{ current.name }</li>
      case page => <li>
        <a href={ Printer.webify(page.name) }>{ page.name }</a> 
      </li>
    } } </ol>

  def print(page: Page) = {
    def lastnext(in: List[Page], last: Option[Page]): (Option[Page], Option[Page]) =
      (in, last) match {
        case (List(l, `page`, n, _*), _) => (Some(l), Some(n))
        case (List(l, `page`), _) => (Some(l), None)
        case (List(`page`, n, _*), _) => (last, Some(n))
        case _  => lastnext(in.tail, last)
      }
    val (prev, next) = lastnext(contents.pages, None)
    <html>
      <head>
        <title>{ "%s: %s".format(contents.title, page.name) }</title>
        <link rel="stylesheet" href="css/blueprint/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="css/blueprint/print.css" type="text/css" media="print"/> 
        <!--[if lt IE 8]>
          <link rel="stylesheet" href="css/blueprint/ie.css" type="text/css" media="screen, projection"/>
        <![endif]-->
        <link rel="stylesheet" href="css/pamflet.css" type="text/css" media="screen, projection"/> 
      </head>
      <body>
        <div class="container">
          <div class="span-20 topnav">
            <div class="span-2">{
              prev.map { p =>
                <a href={ Printer.webify(p.name)}>&lt;</a>
              }.getOrElse { <span>&nbsp;</span> } .toSeq
            }</div>
            <div class="span-16 title">{ contents.title }</div>
            <div class="span-2 last">{
              next.map { n =>
                <a class="pageright" href={ Printer.webify(n.name)}>&gt;</a>
              }.getOrElse { <span>&nbsp;</span> }.toSeq
            }</div>
          </div>
          <div class="span-20 contents">
            { toXHTML(page.blocks) ++ toc(page) }
          </div>
        </div>
      </body>
    </html>
  }

  def named(name: String) =
    contents.pages.find { page =>
      Printer.webify(page.name) == name
    }

  def printNamed(name: String) = named(name).map(print)
}      
    
