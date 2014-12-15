package pamflet
import PamfletDiscounter.toXHTML
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
case class Printer(
  contents: Contents,
  globalContents: GlobalContents,
  manifest: Option[String]
) {
  def defaultLanguage = globalContents.template.defaultLanguage

  def toc(current: Page) = {
    val href: Page => String = current match {
      case _: ScrollPage => (p: Page) => BlockNames.fragment(p.name)
      case _ => (p: Page) => p.pathFromBase
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
      case sect @ Section(_, _, blocks, children, _, _) =>
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
      case _ : DeepContents | _ : ScrollPage => "show"
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
        case _: ScrollPage => contents.pamflet.children.collect{
          case cp: ContentPage => cp
        }
        case _ => contents.pamflet.children
      })}</div></div>
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
          src= { page.pathTo("js/prettify/prettify.js") } ></script> } ++
      page.prettifyLangs.map { br =>
        <script type="text/javascript" src={
          page.pathTo("js/prettify/lang-%s.js".format(br))
        } ></script>
      } ++
      <link type="text/css" rel="stylesheet" href={ page.pathTo("css/prettify.css") } />
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
      <ul class="language-bar">
        {
          val lis = 
            for {
              lang <- globalContents.template.languages
              otherContents <- globalContents.byLanguage.get(lang).toList
              otherPage <- otherContents.pages if otherPage.localPath == page.localPath
            } yield {
              val lang = otherContents.language
              <li>
                <a href={ page.pathTo(otherPage) } ><span class={ "lang-item lang-" + lang }>{ languageName(lang) }</span></a>
              </li>
            }
          if (lis.size < 2) Nil
          else lis
        }
      </ul>
    }

  def header(page: Page): xml.NodeSeq =
    page.template.get("layout.header") map { evalLayout(page)
    } getOrElse {
      <div class="container">
        <div class="span-16 prepend-1 append-1">
          <div class="span-16 top nav">
            <div class="span-16 title">
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
    val s = (contents.layouts filter {
      case(k, _) => k == name
    } map {
      case(_, v) => v
    }).headOption.getOrElse { sys.error(s"$name was not found in layouts!") }
    val m = modifiedTemplate(page)
    val (_, blocks, _) = Knock.knockEither(s, m) match {
      case Right(x) => x
      case Left(x)  =>
        Console.err.println("Error while processing " + x)
        throw x
    }
    toXHTML(blocks)    
  }

  // https://theantlrguy.atlassian.net/wiki/display/ST/StringTemplate+2.2+Documentation
  def modifiedTemplate(page: Page): Template = {
    import java.util.{Map => JMap}
    import collection.JavaConversions._
    val contentsMap: JMap[String, String] = Map("title" -> contents.title)
    val pageMap: JMap[String, String] =
      Map("name" -> page.name) ++
      (if (contents.title != page.name) Map("title" -> page.name)
      else Map())
    page.template.updated(
      Map(
        "contents" -> contentsMap,
        "page" -> pageMap
      )
    )
  }

  def print(page: Page): xml.Node = {
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
    val colorScheme = page.template.get("color_scheme") map {"color_scheme-" + _} getOrElse "color_scheme-redmond"

    val html = <html>
      <head>
        <meta charset="utf-8" />
        <meta content="width=device-width, initial-scale=1" name="viewport" />
        <title>{ "%s — %s".format(contents.title, page.name) }</title>
        {
          contents.favicon match {
            case Some(x) => <link rel="shortcut icon" href="favicon.ico" />
            case None    => Nil
          }
        }
        <link rel="stylesheet" href={ page.pathTo("css/blueprint/screen.css") } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ page.pathTo("css/blueprint/grid.css") } type="text/css" media={bigScreen}/>
        <link rel="stylesheet" href={ page.pathTo("css/blueprint/print.css") } type="text/css" media="print"/> 
        <!--[if lt IE 8]>
          <link rel="stylesheet" href={ page.pathTo("css/blueprint/ie.css") } type="text/css" media="screen, projection"/>
        <![endif]-->
        <link rel="stylesheet" href={ page.pathTo("css/pamflet.css") } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ page.pathTo("css/pamflet-print.css") } type="text/css" media="print"/>
        <link rel="stylesheet" href={ page.pathTo("css/pamflet-grid.css") } type="text/css" media={bigScreen}/>
        <link rel="stylesheet" href={ page.pathTo("css/color_scheme-redmond.css") } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ page.pathTo("css/color_scheme-github.css") } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ page.pathTo("css/color_scheme-monokai.css") } type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href={ page.pathTo("css/" + Heights.heightCssFileName(page)) } type="text/css" media={bigScreen}/>
        <script type="text/javascript" src={ page.pathTo("js/jquery-1.6.2.min.js") }></script>
        <script type="text/javascript" src={ page.pathTo("js/jquery.collapse.js") }></script>
        <script type="text/javascript" src={ page.pathTo("js/pamflet.js") }></script>
        <script type="text/javascript">
          Pamflet.page.language = '{xml.Unparsed(contents.language)}';
        </script>
        {
          prettify(page)
        }
        {
          (contents.css.map { case (filename, contents) =>
            <link rel="stylesheet" href={ "css/" + filename } type="text/css" media="screen, projection"/>
          }) ++
          contents.css.map { case (filename, contents) =>
            <link rel="stylesheet" href={ "css/" + filename } type="text/css" media="screen, projection"/>
          }
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
        { prev.map { p =>
          <a class="page prev nav" href={ page.pathTo(p) }>
            <span class="space">&nbsp;</span>
            <span class="flip arrow">{arrow}</span>
          </a>
        }.toSeq ++
        next.map { n =>
          <a class="page next nav" href={ page.pathTo(n) }>
            <span class="space">&nbsp;</span>
            <span class="arrow">{arrow}</span>
          </a>
        }.toSeq }
        <div class="container contentswrapper">
          <div class="span-16 prepend-1 append-1 contents">
            { page match {
                case page: DeepContents =>
                  toc(page)
                case page: ContentPage =>
                  toXHTML(page.blocks) ++ next.collect {
                    case n: AuthoredPage =>
                      <div class="bottom nav span-16">
                        <em>Next Page</em>
                        <span class="arrow">{arrow}</span>
                        <a href={ page.pathTo(n) }> {n.name} </a>
                        { languageBar(page) }
                      </div>
                    case _ =>
                      <div class="bottom nav end span-16">
                        { languageBar(page) }
                      </div>
                  } ++ toc(page) ++ comment(page)
                case page: ScrollPage =>
                  toc(page) ++ toXHTML(page.blocks)
                case page: FrontPageNews =>
                  toXHTML(page.blocks)
            } }
          </div>
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
               ><img src={ page.pathTo("img/fork.png") } alt="Fork me on GitHub"/></a>
          }.toSeq
        }
        {
          page.template.get("twitter").map { twt =>
            <div class="highlight-outer">
              <div class="highlight-menu">
                <ul>
                  <li><button id="highlight-button-twitter"><img src={ page.pathTo("img/twitter-bird-dark-bgs.png") } /></button></li>
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
}
