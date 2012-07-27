package pamflet
import PamfletDiscounter.toXHTML

object Printer {
  def webify(page: Page) =
    BlockNames.encode(page.template.get("out") getOrElse {
      page.name + ".html"
    })
  /** File names shouldn't be url encoded, just space converted */
  def fileify(page: Page) =
    (page.template.get("out") getOrElse {
      page.name + ".html"
    }).replace(' ', '+')
}
case class Printer(contents: Contents, manifest: Option[String]) {
  def toc(current: Page) = {
    val href: Page => String = current match {
      case ScrollPage(_, _) => (p: Page) => BlockNames.fragment(p.name)
      case _ => Printer.webify
    }
      
    val link: Page => xml.NodeSeq = {
      case `current` =>
        <div class="current">{ current.name }</div>
      case page =>
        { <div><a href={ href(page) }>{ 
          page.name
        }</a></div> } ++ ((page, current) match {
          case (page: ContentPage, c: DeepContents) =>
            Outline(page)
          case _ => Nil
        })
    }
    def draw: Page => xml.NodeSeq = {
      case sect @ Section(blocks, children, _) =>
        link(sect) ++ list(children)
      case page => link(page)
    }
    def list(pages: Seq[Page]) = {
      <ol class="toc"> { pages.map {
        case page: ContentPage => <li>{ draw(page) }</li>
        case page => <li class="generated">{ draw(page) }</li>
       } } </ol>
    }
    def display: String = current match {
      case DeepContents(_) | ScrollPage(_, _) => "show"
      case _ =>
        current.template.get("toc") match {
          case Some("hide") => "hide"
          case Some("collapse") => "collap"
          case _ => "show"
        }
    }
    if (display == "hide") Nil
    else <div class={ "tocwrapper " + display }>
      <a class="tochead nav" style="display: none" href="#toc">❦</a>
      <a name="toc"></a>
      <h4 class="toctitle">Contents</h4>
      <div class="tocbody">
      {link(contents.pamflet) ++
      list(current match {
        case ScrollPage(_, _) => contents.pamflet.children.collect{
          case cp: ContentPage => cp
        }
        case _ => contents.pamflet.children
      })}</div></div>
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
        <script src="js/jquery.collapse.js"/>
        <script src="js/pamflet.js"/>
        {
          prettify(page)
        }
        <meta charset="utf-8" />
        <meta content="width=device-width, initial-scale=1" name="viewport"></meta>
        {
          page.template.get("google-analytics").toList.map { uid: String => 
            <script type="text/javascript">
            var _gaq = _gaq || [];
            _gaq.push(['_setAccount', '{xml.Unparsed(uid)}']);
            _gaq.push(['_trackPageview']);
            (function() {{
              var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
              ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
              var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
            }})();
            </script>
         }
        }
      </head>
      <body>
        { prev.map { p =>
          <a class="page prev nav" href={ Printer.webify(p)}>
            <span class="space">&nbsp;</span>
            <span class="flip">❧</span>
          </a>
        }.toSeq ++
        next.map { n =>
          <a class="page next nav" href={ Printer.webify(n)}>
            <span class="space">&nbsp;</span>
            <span>❧</span>
          </a>
        }.toSeq }
        <div class="container">
          <div class="span-16 prepend-1 append-1">
            <div class="top nav span-16 title">
              <span>{ contents.title }</span> { 
                if (contents.title != page.name)
                  "— " + page.name
                else "" }
            </div>
          </div>
          <div class="span-16 prepend-1 append-1 contents">
            { page match {
                case page: DeepContents =>
                  toc(page)
                case page: ContentPage =>
                  toXHTML(page.blocks) ++ toc(page)
                case page: ScrollPage =>
                  toc(page) ++ toXHTML(page.blocks)
            } }
          </div>
        </div>
        {
          page.template.get("github").map { repo =>
            <a href={"http://github.com/" + repo} class="fork nav"
               ><img src="img/fork.png" alt="Fork me on GitHub"/></a>
          }.toSeq
        }
      </body>
    </html>
    manifest.map { mf => 
      html % new scala.xml.UnprefixedAttribute("manifest", mf, scala.xml.Null)
    } getOrElse { html }
  }

  def named(name: String) =
    contents.pages.find { page =>
      Printer.webify(page) == name
    }

  def printNamed(name: String) = named(name).map(print)
}
