package pamflet
import com.tristanhunt.knockoff._

class Contents(storage: Storage) {
  def contents = storage.items.map { str =>
    val blocks = DefaultDiscounter.knockoff(str)
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
case class Page(name: String, blocks: Seq[Block])
