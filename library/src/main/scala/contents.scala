package pamflet
import com.tristanhunt.knockoff._
import java.net.URI

case class Contents(
  rootSection: Section,
  css: Seq[(String,String)],
  files: Seq[(String, URI)],
  template: Template
) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
  val pamflet = Section(rootSection.blocks,
                        rootSection.children ::: 
                        DeepContents(template) ::
                        ScrollPage(rootSection, template) ::
                        Nil,
                        rootSection.template)
  val pages = traverse(pamflet.children, pamflet :: Nil)
  val title = pamflet.name
  val langs = (Set.empty[String] /: pages) { _ ++ _.langs }
}
sealed trait Page {
  def name: String
  def langs: Set[String]
  def referencedLangs: Set[String]
  def template: Template
}
trait AuthoredPage extends Page {
  def blocks: Seq[Block]
  lazy val referencedLangs =
    (Set.empty[String] /: blocks) {
      case (s, FencedCodeBlock(_, _, Some(lang))) => s + lang
      case (s, _) => s
    }
  lazy val langs = referencedLangs.filter { lang =>
    try {
      new java.net.URL(Shared.resources,
                       "js/prettify/lang-%s.js".format(lang)
                     ).openStream().close()
      true
    } catch {
      case _ => false
    }
  }
}
trait ContentPage extends AuthoredPage {
  lazy val name = BlockNames.name(blocks)
}
case class Leaf(blocks: Seq[Block],
                template: Template) extends ContentPage
object Leaf {
  def apply(t: (Seq[Block], Template)): Leaf = Leaf(t._1, t._2)
}
case class Section(blocks: Seq[Block], 
                   children: List[Page],
                   template: Template) extends ContentPage
case class DeepContents(template: Template) extends Page {
  val name = "Contents in Depth"
  def langs = Set.empty
  def referencedLangs = Set.empty
}
case class ScrollPage(root: Section,
                      template: Template) extends AuthoredPage {
  val name = "Combined Pages"
  def flatten(pages: List[Page]): Seq[Block] =
    pages.view.flatMap {
      case Leaf(blocks, _) => blocks
      case Section(blocks, children, _) =>
        blocks ++: flatten(children)
      case _ => Seq.empty
    }
  def blocks = root.blocks ++: flatten(root.children)
}
