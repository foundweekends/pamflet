package pamflet
import knockoff._
import java.net.URI
import collection.immutable.Map

case class Globalized(
  contents: Map[String, Contents],
  template: Template
) {
  def apply(lang: String): Contents = contents(lang)
  def defaultLanguage: String = template.defaultLanguage
  def languages: collection.Seq[String] = template.languages
  lazy val defaultContents: Contents = apply(defaultLanguage)
}
case class Contents(
  language: String,
  val isDefaultLang: Boolean,
  rootSection: Section,
  css: collection.Seq[(String,String)],
  files: collection.Seq[(String, URI)],
  favicon: Option[URI],
  template: Template,
  layouts: collection.Seq[(String,String)]
) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_,_,_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
  val scrollPage = ScrollPage(rootSection, template)
  val pamflet = Section(rootSection.localPath,
                        rootSection.raw,
                        rootSection.blocks,
                        rootSection.children ::: 
                        DeepContents(template) ::
                        scrollPage ::
                        Nil,
                        rootSection.template)
  val pages = traverse(pamflet.children, pamflet :: Nil)
  val title = pamflet.name
  val prettifyLangs = pages.foldLeft(Set.empty[String]) { _ ++ _.prettifyLangs }
}
sealed trait Page {
  def name: String
  def prettifyLangs: Set[String]
  def referencedLangs: Set[String]
  def localPath: String
  def template: Template
}
sealed trait AuthoredPage extends Page {
  def blocks: collection.Seq[Block]
  // Always reference Scala for fenced plugin purpose
  lazy val referencedLangs =
    blocks.foldLeft(Set("scala")) {
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
                blocks: collection.Seq[Block],
                template: Template) extends ContentPage
object Leaf {
  def apply(localPath: String, t: (String, collection.Seq[Block], Template)): Leaf = Leaf(localPath, t._1, t._2, t._3)
}
case class Section(localPath: String,
                   raw: String,
                   blocks: collection.Seq[Block], 
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
  def flatten(pages: List[Page]): collection.Seq[Block] =
    pages.view.flatMap {
      case Leaf(_, _, blocks, _) => blocks
      case Section(_, _, blocks, children, _) =>
        blocks ++: flatten(children)
      case _ => Seq.empty
    }
  def blocks = root.blocks ++: flatten(root.children)
  def flattenRaw(pages: List[Page]): collection.Seq[String] =
    pages.view.flatMap {
      case Leaf(_, raw, _ , _) => Seq(raw)
      case Section(_, raw, _, children, _) =>
        Seq(raw) ++: flattenRaw(children)
      case _ => Seq("")
    }
  def raw: String = (Seq(root.raw) ++: flattenRaw(root.children)).mkString("\n")
}
