package pamflet
import com.tristanhunt.knockoff._

case class Contents(pamflet: Section, css: Seq[(String,String)]) {
  def traverse(incoming: List[Page], past: List[Page]): List[Page] =
    incoming match {
      case (head @ Section(_,_)) :: tail =>
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
  lazy val name = BlockNames.name(blocks)
}
case class Leaf(blocks: Seq[Block]) extends Page
case class Section(blocks: Seq[Block], 
                   children: List[Page]) extends Page
