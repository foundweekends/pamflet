package pamflet
import com.tristanhunt.knockoff._

case class Contents(pamflet: Section, css: Seq[(String,String)]) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_,_)) :: tail =>
        traverse(head.children ::: tail, head :: past)
      case head :: tail =>
        traverse(tail, head :: past)
      case Nil => past.reverse
    }
    
  val pages = traverse(pamflet.children, pamflet :: Nil)
  val title = pamflet.name
  val langs = (Set.empty[String] /: pages) { _ ++ _.langs }
}
sealed trait Page {
  def name: String
  def blocks: Seq[Block]
  lazy val langs =
    (Set.empty[String] /: blocks) {
      case (s, FencedCodeBlock(_, _, Some(lang))) => s + lang
      case (s, _) => s
    } filter { lang =>
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
case class Leaf(name: String, blocks: Seq[Block]) extends Page
object Leaf {
  def apply(blocks: Seq[Block]): Leaf = Leaf(Page.name(blocks), blocks)
}
case class Section(name: String, 
                   blocks: Seq[Block], 
                   children: List[Page]) extends Page
object Page {
  def name(blocks: Seq[Block]) =
    blocks.projection.flatMap {
      case h: Header => h.spans.flatMap {
        case t: Text => Seq(t.content)
        case _ => Seq()
      }
      case _ => Seq()
    }.firstOption.getOrElse { "Untitled" }
}
