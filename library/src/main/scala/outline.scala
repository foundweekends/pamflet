package pamflet
import com.tristanhunt.knockoff._
import PamfletDiscounter.headerText

object Outline {
  def apply(blocks: Seq[Block]) =
    <ol> {
      build(blocks.view.collect { case h: Header => h }, 0).nodes
    } </ol>
  private case class Return(nodes: xml.NodeSeq, rest: Seq[Header])
  private def build(blocks: Seq[Header], cur: Int): Return =
    blocks match {
      case Seq(a, b, tail @_*) if a.level == cur && b.level > cur =>
        val nested = build(b +: tail, b.level)
        val after = build(nested.rest, cur)
        Return((
          <li> { headerText(a.spans) }
            <ol> { nested.nodes } </ol>
          </li>
        ) ++ after.nodes, after.rest)
      case Seq(a, tail @ _*) if a.level > cur =>
        val Return(nodes, rest) = build(blocks, a.level)
        Return(nodes, rest)
      case Seq(a, tail @ _*) if a.level == cur =>
        val Return(nodes, rest) = build(tail, cur)
        Return(( <li> { headerText(a.spans) } </li> ) ++ nodes, rest)
      case _ =>
        Return(Seq.empty, blocks)
    }
}
