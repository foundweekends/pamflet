package pamflet
import com.tristanhunt.knockoff._

case class Contents(rootSection: Section, css: Seq[(String,String)]) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
  val pamflet = Section(rootSection.blocks,
                        rootSection.children ::: 
                        DeepContents ::
                        ScrollPage(rootSection) ::
                        Nil)
  val pages = traverse(pamflet.children, pamflet :: Nil)
  val title = pamflet.name
  val langs = (Set.empty[String] /: pages) { _ ++ _.langs }
}
sealed trait Page {
  def name: String
  def langs: Set[String]
  def referencedLangs: Set[String]
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
case class Leaf(blocks: Seq[Block]) extends ContentPage
case class Section(blocks: Seq[Block], 
                   children: List[Page]) extends ContentPage
object DeepContents extends Page {
  val name = "Contents in Depth"
  def langs = Set.empty
  def referencedLangs = Set.empty
}
case class ScrollPage(root: Section) extends AuthoredPage {
  val name = "Combined Pages"
  def flatten(pages: List[Page]): Seq[Block] =
    pages.view.flatMap {
      case Leaf(blocks) => blocks
      case Section(blocks, children) =>
        blocks ++: flatten(children)
      case _ => Seq.empty
    }
  def blocks = root.blocks ++: flatten(root.children)
}
