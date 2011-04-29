package pamflet
import com.tristanhunt.knockoff._

class Contents(storage: Storage) {
  val pages = storage.items.map { str =>
    Page(DefaultDiscounter.knockoff(str))
  }.toList
  val title = pages.headOption.map {
    case Page(name, _) => name
  }.getOrElse { "Empty" }
}
case class Page(name: String, blocks: Seq[Block])
object Page {
  def apply(blocks: Seq[Block]): Page = {
    val name = blocks.view.collect {
      case h: Header => h
    }.flatMap {
      _.spans.collect {
        case t: Text => t
      }.headOption.map { _.content }
    }.headOption.getOrElse { "Untitled" }
    Page(name, blocks)
  }
}
