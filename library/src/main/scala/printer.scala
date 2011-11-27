package pamflet
import PamfletDiscounter.toXHTML

object Printer {
  def webify(name: String) = BlockNames.encode(name) + ".html"
  /** File names shouldn't be url encoded, just space converted */
  def fileify(name: String) =
    name.replace(' ', '+') + ".html"
}
case class Printer(contents: Contents, manifest: Option[String]) {
  def toc(current: Page) = {
    val href: String => String = current match {
      case ScrollPage(_) => BlockNames.fragment
      case _ => Printer.webify
    }
      
    val link: Page => xml.NodeSeq = {
      case `current` =>
        <div class="current">{ current.name }</div>
      case page =>
        { <div><a href={ href(page.name) }>{ 
          page.name 
        }</a></div> } ++ (page match {
          case page: ContentPage if current == DeepContents =>
            Outline(page)
          case _ => Nil
        })
    }
    def draw: Page => xml.NodeSeq = {
      case sect @ Section(blocks, children) =>
        link(sect) ++ list(children)
      case page => link(page)
    }
    def list(pages: Seq[Page]) = {
      <ol class="toc"> { pages.map { page =>
        <li>{ draw(page) }</li>
       } } </ol>
    }

    <h4>Contents</h4> ++
    link(contents.pamflet) ++
    list(current match {
      case ScrollPage(_) => contents.pamflet.children.collect{
        case cp: ContentPage => cp
      }
      case _ => contents.pamflet.children
    })
  }

  def prettify(page: Page) = {
    page.referencedLangs.find{ _ => true }.map { _ =>
      { <script type="text/javascript"
          src="js/prettify/prettify.js" /> } ++
      page.langs.map { br =>
        <script type="text/javascript" src={
          "js/prettify/lang-%s.js".format(br)
        } />
      } ++
      <link type="text/css" rel="stylesheet" href="css/prettify.css"/>
      <script type="text/javascript"><!--
        window.onload=function() { prettyPrint(); };
      --></script>
    }.toSeq
  }

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
    val bigScreen = "screen and (min-device-width: 800px), projection"
    
    val html = <html>
      <head>
        <title>{ "%s — %s".format(contents.title, page.name) }</title>
        <link rel="stylesheet" href="css/blueprint/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="css/blueprint/grid.css" type="text/css" media={bigScreen}/>
        <link rel="stylesheet" href="css/blueprint/print.css" type="text/css" media="print"/> 
        <!--[if lt IE 8]>
          <link rel="stylesheet" href="css/blueprint/ie.css" type="text/css" media="screen, projection"/>
        <![endif]-->
        <link rel="stylesheet" href="css/pamflet.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="css/pamflet-print.css" type="text/css" media="print"/>
        <link rel="stylesheet" href="css/pamflet-grid.css" type="text/css" media={bigScreen}/>
        {
          contents.css.map { case (filename, contents) =>
            <link rel="stylesheet" href={"css/" + filename} type="text/css" media="screen, projection"/>
          }
        }
        <script src="js/jquery-1.6.2.min.js"/>
        <script src="js/pamflet.js"/>
        {
          prettify(page)
        }
        <meta charset="utf-8" />
        <meta content="width=device-width, initial-scale=1" name="viewport"></meta>
      </head>
      <body>
        { prev.map { p =>
          <a class="page prev nav" href={ Printer.webify(p.name)}>
            <span class="space">&nbsp;</span>
            <span class="flip">❧</span>
          </a>
        }.toSeq ++
        next.map { n =>
          <a class="page next nav" href={ Printer.webify(n.name)}>
            <span class="space">&nbsp;</span>
            <span>❧</span>
          </a>
        }.toSeq }
        <div class="container">
          <div class="span-16 prepend-1 append-1">
            <div class="top nav span-16 title">
              <span>{ contents.title }</span>
              { if (contents.title != page.name)
                  " — " + page.name
                else ""
              }
            </div>
          </div>
          <div class="span-16 prepend-1 append-1 contents">
            { page match {
                case DeepContents =>
                  toc(page)
                case page: ContentPage =>
                  toXHTML(page.blocks) ++ toc(page)
                case page: ScrollPage =>
                  toc(page) ++ toXHTML(page.blocks) ++
                    <a class="nav" href={
                      Printer.webify(contents.title)
                    }><em>Combined Pages</em></a>
            } }
          </div>
        </div>
      </body>
    </html>
    manifest.map { mf => 
      html % new scala.xml.UnprefixedAttribute("manifest", mf, scala.xml.Null)
    } getOrElse { html }
  }

  def named(name: String) =
    contents.pages.find { page =>
      Printer.webify(page.name) == name
    }

  def printNamed(name: String) = named(name).map(print)
}
