package pamflet
import Knock.discounter.toXHTML
import collection.immutable.Map

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

case class Printer(contents: Contents, globalized: Globalized, manifest: Option[String]) {
  def defaultLanguage = globalized.defaultLanguage
  val relativeBase: String = relative(defaultLanguage)
  def relative(lang: String): String =
    if (contents.isDefaultLang) {
      if (lang == defaultLanguage) ""
      else lang + "/"
    }
    else {
      if (lang == defaultLanguage) "../"
      else "../" + lang + "/"
    }

  def tocDisplay(current: Page): TocType =
    current match {
      case DeepContents(_) | ScrollPage(_, _) => TocType.Left
      case _ =>
        current.template.get("toc") match {
          case Some("left")     => TocType.Left
          case Some("hide")     => TocType.Hide
          case Some("collapse") => TocType.Collapse
          case Some("bottom")   => TocType.Bottom
          case _                => TocType.Left
        }
    }
  def toc(current: Page, leftToc: Boolean) = {
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
          case (page: ContentPage, _: DeepContents) => Outline(page)
          case _                                    => Nil
        })
    }
    def draw: Page => xml.NodeSeq = {
      case sect @ Section(_, _, _, children, _) => link(sect) ++ list(children)
      case page                                 => link(page)
    }
    def list(pages: Seq[Page]) = {
      <ol class="toc"> { pages.map {
        case page: ContentPage => <li>{ draw(page) }</li>
        case page => <li class="generated">{ draw(page) }</li>
       } } </ol>
    }
    val display = tocDisplay(current)
    def tocdiv =
      <div class="tocwrapper">
        {
          if (display == TocType.Collapse) <a class="tochead nav" href="#toc" data-toggle="collapse" aria-expanded="false" aria-controls="toc">❦</a>
          else Nil
        }
        <div class={ "tocbody " + display.css } id="toc">
        {
          if (leftToc) Nil
          else <h4 class="toctitle">Contents</h4>
        }
        {link(contents.pamflet) ++
        list(current match {
          case ScrollPage(_, _) => contents.pamflet.children.collect{
            case cp: ContentPage => cp
          }
          case _ => contents.pamflet.children
        })}</div>
      </div>

    if (display == TocType.Hide) Nil
    else if (leftToc) <div class="lefttocwrapper">
      {tocdiv}
      </div>
    else tocdiv
  }
  def comment(current: Page) = {
    current.template.get("disqus") map { disqusName =>
      val disqusCode = """
        var disqus_shortname = '""" + disqusName + """';
        (function() {
            var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
            dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';
            (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
        })();
"""

      { <div id="disqus_thread"></div> } ++
      { <script type="text/javascript">{disqusCode}</script> }
    } getOrElse Nil
  }
  def prettify(page: Page) = {
    page.referencedLangs.find{ _ => true }.map { _ =>
      { <script type="text/javascript"
          src= { relativeBase + "js/prettify/prettify.js" } ></script> } ++
      page.prettifyLangs.map { br =>
        <script type="text/javascript" src={
          relativeBase + "js/prettify/lang-%s.js".format(br)
        } ></script>
      } ++
      <link type="text/css" rel="stylesheet" href={ relativeBase + "css/prettify.css" } />
      <script type="text/javascript"><!--
        window.onload=function() { prettyPrint(); };
      --></script>
    }.toSeq
  }

  def languageBar(page: Page) = 
    if (page.template.languages.size < 2) Nil
    else {
      def languageName(langCode: String): String =
        page.template.get("lang-" + langCode) getOrElse {
          Language.languageName(langCode) getOrElse langCode
        }
      <div class="row w-100">
        <div class="col-md-auto ml-auto">
          <ul class="language-bar">
            {
              val lis = 
                for {
                  lang <- page.template.languages
                  p <- globalized(lang).pages.find { _.localPath == page.localPath }
                } yield <li><a href={ relative(lang) + Printer.webify(p) } ><span class={ "lang-item lang-" + lang }>{languageName(lang)}</span></a></li>
              if (lis.size < 2) Nil
              else lis
            }
          </ul>
        </div>
      </div>
    }

  def header(page: Page): xml.NodeSeq =
    page.template.get("layout.header") map { evalLayout(page)
    } getOrElse {
      <div class="container-fluid top nav">
        <div class="row justify-content-md-center w-100">
          <div class="col-md-auto">
            <div class="title">
              <span>{ contents.title }</span> { 
                if (contents.title != page.name)
                  "— " + page.name
                else "" }
            </div>
          </div>
        </div>
      </div>
    }

  def footer(page: Page): xml.NodeSeq =
    page.template.get("layout.footer") map { evalLayout(page)
    } getOrElse { Nil }

  def evalLayout(page: Page)(name: String): xml.NodeSeq = {
    val s = (contents.layouts filter { case(k, _) => k == name } map { case(_, v) => v }).headOption.orElse({
      if (contents.isDefaultLang) None
      else (globalized.defaultContents.layouts filter { case(k, _) => k == name } map { case(_, v) => v }).headOption
    }).getOrElse { sys.error(s"$name was not found in layouts!") }
    val m = modifiedTemplate(page)
    val (_, blocks, _) = Knock.knockEither(s, m) match {
      case Right(x) => x
      case Left(x)  =>
        Console.err.println("Error while processing " + x)
        // x.printStackTrace()
        throw x
    }
    toXHTML(blocks)    
  }

  // https://theantlrguy.atlassian.net/wiki/display/ST/StringTemplate+2.2+Documentation
  def modifiedTemplate(page: Page): Template = {
    import java.util.{Map => JMap}
    import collection.JavaConverters._
    val contentsMap: JMap[String, String] = Map("title" -> contents.title).asJava
    val pageMap =
      Map(
        "name" -> page.name,
        "localPath" -> page.localPath
      ) ++
      (if (contents.title != page.name) Map("title" -> page.name)
      else Map())
    page.template.updated(
      Map(
        "contents" -> contentsMap,
        "page" -> pageMap.asJava
      )
    )
  }

  def print(page: Page) = {
    val tocd = tocDisplay(page)
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
   
    val arrow = page.template.get("pamflet.arrow") getOrElse "❧"
    val colorScheme = page.template.get("color_scheme") map {"color_scheme-" + _} getOrElse "color_scheme-github"

    def mainContents =
      page match {
        case page: DeepContents =>
          toc(page, false)
        case page: ContentPage =>
          toXHTML(page.blocks) ++ next.collect {
            case n: AuthoredPage =>
              <div class="bottom nav">
                <div class="row">
                  <div class="col-md-auto">
                    <a href={Printer.webify(n)}>
                      <div class="arrowitem">
                        <span class="arrow">{arrow}</span>
                      </div>

                      <div class="arrowitem">
                        <em>Next page</em><br/>
                        {n.name}
                      </div>

                    </a>
                  </div>
                </div>
                { languageBar(page) }
              </div>
            case _ =>
              <div class="bottom nav end row">
                { languageBar(page) }
              </div>
          } ++ {
            if (tocd == TocType.Left) Nil
            else toc(page, false)
          } ++ comment(page)
        case page: ScrollPage =>
          toc(page, false) ++ toXHTML(page.blocks)
      }

    val html = <html>
      <head>
        <meta charset="utf-8" />
        <meta content="width=device-width, initial-scale=1" name="viewport" />
        <title>{ "%s — %s".format(contents.title, page.name) }</title>
        {
          contents.favicon match {
            case Some(_) => <link rel="shortcut icon" href="favicon.ico" />
            case None    =>
              if (contents.isDefaultLang) Nil
              else {
                globalized.defaultContents.favicon match {
                  case Some(_) => <link rel="shortcut icon" href= { relativeBase + "favicon.ico" } />
                  case None    => Nil
                }
              }
          }
        }
        <link rel="stylesheet" href={ relativeBase + "css/bootstrap.min.css" } type="text/css"/>
        <link rel="stylesheet" href={ relativeBase + "css/pamflet.css" } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ relativeBase + "css/pamflet-print.css" } type="text/css" media="print"/>
        <link rel="stylesheet" href={ relativeBase + "css/pamflet-grid.css" } type="text/css" media={bigScreen}/>
        <link rel="stylesheet" href={ relativeBase + "css/color_scheme-redmond.css" } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ relativeBase + "css/color_scheme-github.css" } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ relativeBase + "css/color_scheme-monokai.css" } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ relativeBase + "css/" + Heights.heightCssFileName(page) } type="text/css" media={bigScreen}/>
        <script type="text/javascript" src={ relativeBase + "js/jquery-3.3.1.min.js" }></script>
        <script type="text/javascript" src={ relativeBase + "js/bootstrap.bundle.min.js" }></script>
        <script type="text/javascript" src={ relativeBase + "js/pamflet.js" }></script>
        <script type="text/javascript">
          Pamflet.page.language = '{xml.Unparsed(contents.language)}';
        </script>
        {
          prettify(page)
        }
        {
          (globalized.defaultContents.css.map { case (filename, _) =>
            <link rel="stylesheet" href={ relativeBase + "css/" + filename } type="text/css" media="screen, projection"/>
          }) ++
          (if (contents.isDefaultLang) Nil
          else contents.css.map { case (filename, _) =>
            <link rel="stylesheet" href={ "css/" + filename } type="text/css" media="screen, projection"/>
          })
        }
        {
          page.template.get("google-analytics").toList.map { uid: String => 
            // do NOT enclose the script with XML comment. it'll disable Scala embedding {xml.Unparsed(uid)}!
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
        {
          page.template.get("twitter").map { twt =>
            <script type="text/javascript">
              Pamflet.twitter = '{xml.Unparsed(twt)}';
            </script>
          }.toSeq
        }
      </head>
      <body class={colorScheme}>
        <div class="container-fluid contentswrapper h-100">
          <div class="row minh-100">
          { page match {
              case page: ContentPage if tocd == TocType.Left =>
                <div class="col-md-4 col-xl-3 toccolumn leftcolumn">
                  { toc(page, true) }
                </div>
                <div class="col-md-8 col-xs-9">
                  <div class="rightcolumn contents">
                  { mainContents }
                  </div>
                </div>
              case _ =>
                <div class="col-md-4 col-xl-3 leftcolumn">&nbsp;</div>
                <div class="col-md-8 col-xs-9">
                  <div class="rightcolumn contents">
                    { mainContents }
                  </div>
                </div>
          } }
          </div> <!-- row -->
        </div>
        <div class="header">
          {
            header(page)
          }
        </div>
        <div class="footer">
          {
            footer(page)
          }
        </div>
        {
          page.template.get("github").map { repo =>
            <a href={"http://github.com/" + repo} class="fork nav"
               ><img src={ relativeBase + "img/fork.png" } alt="Fork me on GitHub"/></a>
          }.toSeq
        }
        {
          page.template.get("twitter").map { twt =>
            <div class="highlight-outer">
              <div class="highlight-menu">
                <ul>
                  <li><button id="highlight-button-twitter"><img src={ relativeBase + "img/twitter-bird-dark-bgs.png" } /></button></li>
                </ul>
              </div>
            </div>
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
