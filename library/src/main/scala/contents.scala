package pamflet
import com.tristanhunt.knockoff._
import scala.util.parsing.input.{NoPosition}
import com.github.nscala_time.time.Imports._
import java.net.URI
import collection.immutable.Map

case class Globalized(
  contents: Map[String, Contents],
  template: Template
) {
  def apply(lang: String): Contents = contents(lang)
  def defaultLanguage: String = template.defaultLanguage
  def languages: Seq[String] = template.languages
  lazy val defaultContents: Contents = apply(defaultLanguage)
}
case class Contents(
  language: String,
  val isDefaultLang: Boolean,
  pamflet: Page,
  css: Seq[(String,String)],
  files: Seq[(String, URI)],
  favicon: Option[URI],
  template: Template,
  layouts: Seq[(String,String)]
) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_,_,_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
  val pages = traverse(pamflet.children, pamflet :: Nil)
  val title = pamflet.name
  val prettifyLangs = (Set.empty[String] /: pages) { _ ++ _.prettifyLangs }
}
sealed trait Page {
  def name: String
  def prettifyLangs: Set[String]
  def referencedLangs: Set[String]
  def localPath: String
  def template: Template
  def children: List[Page]
  def webPath : String
}
trait BasePathPage { self: Page =>
  def webPath = Printer.webify(this)
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
                template: Template) extends ContentPage with BasePathPage {
  val children = Nil
}
object Leaf {
  def apply(localPath: String, t: (String, Seq[Block], Template)): Leaf = Leaf(localPath, t._1, t._2, t._3)
}
case class Section(localPath: String,
                   raw: String,
                   blocks: Seq[Block], 
                   children: List[Page],
                   template: Template) extends ContentPage with BasePathPage

case class DeepContents(template: Template) extends Page with BasePathPage {
  val name = "Contents in Depth"
  val localPath = name
  val children = Nil
  def prettifyLangs = Set.empty
  def referencedLangs = Set.empty
}
case class ScrollPage(root: Section,
                      template: Template) extends AuthoredPage with BasePathPage {
  val name = "Combined Pages"
  val localPath = name
  val children = Nil
  def flatten(pages: List[Page]): Seq[Block] =
    pages.view.flatMap {
      case Leaf(_, _, blocks, _) => blocks
      case Section(_, _, blocks, children, _) =>
        blocks ++: flatten(children)
      case _ => Seq.empty
    }
  def blocks = root.blocks ++: flatten(root.children)
  def flattenRaw(pages: List[Page]): Seq[String] =
    pages.view.flatMap {
      case Leaf(_, raw, _ , _) => Seq(raw)
      case Section(_, raw, _, children, _) =>
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
  template: Template
) extends ContentPage {
  val children = Nil
  val dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd")
  def webPath = dateFormat.print(date) + "/" + Printer.webify(this)
}

case class FrontPageNews
(
  pages: Stream[NewsStory],
  template: Template
) extends Page with BasePathPage {
  lazy val name = template.get("name").getOrElse("News")
  lazy val localPath = name
  lazy val children = pages.toList
  def prettifyLangs = Set.empty[String]
  def referencedLangs = Set.empty[String]
  val blocks = HTMLBlock(
    (<ul class="news">
      { pages.take(50).map { page =>
        <li><a href={ page.webPath } class="button">
          <h4>
            <span class="name">{ page.name }</span>
            <div class="date small">{
              DateTimeFormat.longDate.print(page.date)
            }</div>
          </h4>
        </a></li>
      } }
    </ul>).toString, NoPosition
  ) :: Nil
}
