package pamflet
import com.tristanhunt.knockoff._

case class Contents(items: Seq[CharSequence], css: Seq[(String,String)]) {
  val pages = items.map { str =>
    Page(PamfletDiscounter.knockoff(str))
  }.toList
  val title = pages.firstOption.map {
    case Page(name, _) => name
  }.getOrElse { "Empty" }
  val langs = (Set.empty[String] /: pages) { _ ++ _.langs }
}
case class Page(name: String, blocks: Seq[Block]) {
  val langs =
    (Set.empty[String] /: blocks) {
      case (s, FencedCodeBlock(_, _, Some(lang))) => s + lang
      case (s, _) => s
    }
}
object Page {
  def apply(blocks: Seq[Block]): Page = {
    val name = blocks.projection.flatMap {
      case h: Header => h.spans.flatMap {
        case t: Text => Seq(t.content)
        case _ => Seq()
      }
      case _ => Seq()
    }.firstOption.getOrElse { "Untitled" }
    Page(name, blocks)
  }
}
