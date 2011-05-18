package pamflet
import com.tristanhunt.knockoff.DefaultDiscounter.toXHTML

object Printer {
  def webify(name: String) =
    java.net.URLEncoder.encode(name, "utf-8") + ".html"
  /** File names shouldn't be url encoded, just space converted */
  def fileify(name: String) =
    name.replace(' ', '+') + ".html"
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
        case ((_ :: tail), _) => lastnext(tail, last)
        case _  => (None, None)
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
        {
          contents.css.map { case (filename, contents) =>
            <link rel="stylesheet" href={"css/" + filename} type="text/css" media="screen, projection"/>
          }
        }
      </head>
      <body>
        { prev.map { p =>
          <a class="page prev" href={ Printer.webify(p.name)}>
            <span class="space">&nbsp;</span>
            <span class="flip">❧</span>
          </a>
        }.toSeq ++
        next.map { n =>
          <a class="page next" href={ Printer.webify(n.name)}>
            <span class="space">&nbsp;</span>
            <span>❧</span>
          </a>
        }.toSeq }
        <div class="container">
          <div class="span-16 prepend-1 append-1 topnav">
            <div class="span-16 title">
              { contents.title }
            </div>
          </div>
          <div class="span-16 prepend-1 append-1 contents">
            { toXHTML(page.blocks) }
            <h5>Contents</h5>
            { toc(page) }
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
    
