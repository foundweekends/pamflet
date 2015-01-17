package pamflet
import com.tristanhunt.knockoff._
import scala.util.parsing.input.{NoPosition}
import com.github.nscala_time.time.Imports._
import java.net.URI
import collection.immutable.Map

case class GlobalContents(
  byLanguage: Map[String, Contents],
  template: Template
)

case class Contents(
  language: String,
  isDefaultLanguage: Boolean,
  pamflet: Page with FrontPage,
  css: Seq[(String,String)],
  files: Seq[(String, URI)],
  favicon: Option[URI],
  layouts: Seq[(String,String)]
) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_,_,_,_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
  val pages = traverse(pamflet.children, pamflet :: Nil)
  val title = pamflet.name
  val prettifyLangs = (Set.empty[String] /: pages) { _ ++ _.prettifyLangs }
  val parents =
    if (isDefaultLanguage) Nil
    else language :: Nil
}

sealed trait Page {
  def name: String
  def prettifyLangs: Set[String]
  def referencedLangs: Set[String]
  def localPath: String
  def template: Template
  def children: List[Page]

  def contentParents: List[String]
  def parents: List[String]
  def webname: String = Printer.webify(this)
  def pathTo(fromBase: String) =
    (parents.map(_ => "..") :+ fromBase).mkString("/")
  def pathTo(other: Page): String = {
    val sharedlen = parents.zip(other.parents).takeWhile(p => p._1 == p._2).size
    (List.fill(parents.length - sharedlen)("..") ::: 
      other.parents.drop(sharedlen) ::: other.webname :: Nil
    ).mkString("/")
  }
  def pathFromBase = (parents ::: webname :: Nil).mkString("/")
}
trait FrontPage { self: Page =>
  def toc(contents: Contents, current: Page): Seq[xml.Node]
}
trait FlatWebPaths { self: Page =>
  val parents = contentParents
}
trait StructuredContents { self: Page with FrontPage =>
  def toc(contents: Contents, current: Page) = {
    val href: Page => String = current match {
      case _: ScrollPage => (p: Page) => BlockNames.fragment(p.name)
      case _ => (p: Page) => current.pathTo(p)
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
}
sealed trait AuthoredPage extends Page {
  def blocks: Seq[Block]
  lazy val referencedLangs =
    (Set.empty[String] /: blocks) {
      case (s, FencedCodeBlock(_, _, Some(lang))) => s + lang
      case (s, _) => s
    }
  lazy val prettifyLangs = referencedLangs.filter { lang =>
    try {
      new java.net.URL(Shared.resources,
                       "js/prettify/lang-%s.js".format(lang)
                     ).openStream().close()
      true
    } catch {
      case _: Throwable => false
    }
  }
}
trait ContentPage extends AuthoredPage {
  lazy val name = BlockNames.name(blocks)
}
case class Leaf(localPath: String,
                raw: String,
                blocks: Seq[Block],
                template: Template,
                contentParents: List[String]) extends ContentPage with FlatWebPaths {
  val children = Nil
}
object Leaf {
  def apply(localPath: String, t: (String, Seq[Block], Template), p: List[String]): Leaf = Leaf(localPath, t._1, t._2, t._3, p)
}
case class Section(localPath: String,
                   raw: String,
                   blocks: Seq[Block], 
                   children: List[Page],
                   template: Template,
                   contentParents: List[String])
extends ContentPage with FlatWebPaths with FrontPage with StructuredContents

case class DeepContents(template: Template,
                        contentParents: List[String])
extends Page with FlatWebPaths {
  val name = "Contents in Depth"
  val localPath = name
  val children = Nil
  def prettifyLangs = Set.empty
  def referencedLangs = Set.empty
}
case class ScrollPage(root: Section,
                      template: Template,
                      contentParents: List[String])
extends AuthoredPage with FlatWebPaths {
  val name = "Combined Pages"
  val localPath = name
  val children = Nil
  def flatten(pages: List[Page]): Seq[Block] =
    pages.view.flatMap {
      case Leaf(_, _, blocks, _, _) => blocks
      case Section(_, _, blocks, children, _, _) =>
        blocks ++: flatten(children)
      case _ => Seq.empty
    }
  def blocks = root.blocks ++: flatten(root.children)
  def flattenRaw(pages: List[Page]): Seq[String] =
    pages.view.flatMap {
      case Leaf(_, raw, _ , _, _) => Seq(raw)
      case Section(_, raw, _, children, _, _) =>
        Seq(raw) ++: flattenRaw(children)
      case _ => Seq("")
    }
  def raw: String = (Seq(root.raw) ++: flattenRaw(root.children)).mkString("\n")
}

case class NewsStory(
  localPath: String,
  raw: String,
  blocks: Seq[Block],
  date: LocalDate,
  template: Template,
  contentParents: List[String]
) extends ContentPage {
  val children = Nil
  val parents = contentParents ::: 
    Format.y.print(date) :: Format.m.print(date) :: Format.d.print(date) ::  Nil
}

object Format {
  val y = DateTimeFormat.forPattern("yyyy")
  val m = DateTimeFormat.forPattern("MM")
  val d = DateTimeFormat.forPattern("dd")
}

case class FrontPageNews
(
  pages: Stream[NewsStory],
  blocks: Seq[Block],
  template: Template,
  contentParents: List[String]
) extends ContentPage with FlatWebPaths with FrontPage {
  lazy val localPath = name
  lazy val children = pages.toList

  def toc(contents: Contents, current: Page) = storyList(current)

  def href(page: Page, current: Page)(inner: Seq[xml.Node]) =
    if (page != current) {
      <a href={ page.pathFromBase } class="button"> { inner } </a>
    } else inner

  def storyList(current: Page) =
    <ul class="news">
      <li> {
        href(this, current) {
          <h4>
            <span class="name">{ this.name }</span>
          </h4>
        }
      } </li>
      {
        pages.take(50).map { page =>
        <li>
          {
            href(page, current) {
              <h4>
                <span class="name">{ page.name }</span>
                <div class="date small">{
                  DateTimeFormat.longDate.print(page.date)
                }</div>
              </h4>
            }
          }
        </li>
      } }
    </ul>
}
