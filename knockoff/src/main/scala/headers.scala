package pamflet

import scala.xml.Node
import com.tristanhunt.knockoff._
import java.net.URLEncoder

trait IdentifiedHeaders extends Discounter { self: TextWriter =>
  def headerText( spans : Seq[Span] ) : String = {
    val stringWriter = new java.io.StringWriter
    spans.map( self.spanToText(_)(stringWriter) )
    return stringWriter.toString        
  }
  override def headerToXHTML = (level, spans) => {
    val spanned = spans.map(spanToXHTML)
    val name = BlockNames.encode(BlockNames.textOf(spans))
    level match {
      case 1 => <h1 id={name}>{ spanned }</h1>
      case 2 => <h2 id={name}>{ spanned }</h2>
      case 3 => <h3 id={name}>{ spanned }</h3>
      case 4 => <h4 id={name}>{ spanned }</h4>
      case 5 => <h5 id={name}>{ spanned }</h5>
      case 6 => <h6>{ spanned }</h6>
      case _ =>
        <div class={ "header" + level } id={name}>{ spanned }</div>
    }
  }
}

object BlockNames {
  def encode(str: String) =
    java.net.URLEncoder.encode(str.trim(), "utf-8")
  def fragment(str: String) = "#" + encode(str)
  def textOf(spans: Seq[Span]) =
    spans.flatMap {
      case t: Text => Seq(t.content)
      case h: HTMLSpan => Seq(h.html)
      case _ => Seq()
    }.mkString("")      
  def name(blocks: Seq[Block]) =
    blocks.view.collect {
      case h: Header => textOf(h.spans)
    }.headOption.getOrElse { "Untitled" }
}
