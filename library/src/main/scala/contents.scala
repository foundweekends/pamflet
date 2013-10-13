package pamflet
import com.tristanhunt.knockoff._
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
  rootSection: Section,
  css: Seq[(String,String)],
  files: Seq[(String, URI)],
  favicon: Option[URI],
  template: Template
) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_,_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
  val pamflet = Section(rootSection.localPath,
                        rootSection.blocks,
                        rootSection.children ::: 
                        DeepContents(template) ::
                        ScrollPage(rootSection, template) ::
                        Nil,
                        rootSection.template)
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
                blocks: Seq[Block],
                template: Template) extends ContentPage
object Leaf {
  def apply(localPath: String, t: (Seq[Block], Template)): Leaf = Leaf(localPath, t._1, t._2)
}
case class Section(localPath: String,
                   blocks: Seq[Block], 
                   children: List[Page],
                   template: Template) extends ContentPage
case class DeepContents(template: Template) extends Page {
  val name = "Contents in Depth"
  val localPath = name
  def prettifyLangs = Set.empty
  def referencedLangs = Set.empty
}
case class ScrollPage(root: Section,
                      template: Template) extends AuthoredPage {
  val name = "Combined Pages"
  val localPath = name
  def flatten(pages: List[Page]): Seq[Block] =
    pages.view.flatMap {
      case Leaf(_, blocks, _) => blocks
      case Section(_, blocks, children, _) =>
        blocks ++: flatten(children)
      case _ => Seq.empty
    }
  def blocks = root.blocks ++: flatten(root.children)
}
