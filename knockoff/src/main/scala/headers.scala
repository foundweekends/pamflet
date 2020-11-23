package pamflet

import knockoff._

trait IdentifiedHeaders extends Discounter with XHTMLWriter { self: TextWriter =>
  def headerText( spans : collection.Seq[Span] ) : String = {
    val stringWriter = new java.io.StringWriter
    spans.map( self.spanToText(_)(stringWriter) )
    stringWriter.toString
  }
  override def headerToXHTML = (level, spans) => {
    val name = BlockNames.encode(BlockNames.textOf(spans))
    val spanned = spans.map(spanToXHTML)
    val anchored = spanned ++
      <a href={ "#" + name } class="header-link"><span class="header-link-content">&nbsp;</span></a>
    level match {
      case 1 => <h1 id={name}>{ anchored }</h1>
      case 2 => <h2 id={name}>{ anchored }</h2>
      case 3 => <h3 id={name}>{ anchored }</h3>
      case 4 => <h4 id={name}>{ anchored }</h4>
      case 5 => <h5 id={name}>{ anchored }</h5>
      case 6 => <h6>{ spanned }</h6>
      case _ =>
        <div class={ "header" + level }>{ spanned }</div>
    }
  }
}

object BlockNames {
  /** Do not generate ids for higher levels than this */
  val maxLevel = 5
  def encode(str: String) =
    java.net.URLEncoder.encode(str.trim(), "utf-8")
  def fragment(str: String) = "#" + encode(str)
  def textOf(spans: collection.Seq[Span]) =
    spans.flatMap {
      case t: Text => Seq(t.content)
      case h: HTMLSpan => Seq(h.html)
      case _ => Seq()
    }.mkString("")      
  def name(blocks: collection.Seq[Block]) =
    blocks.view.collect {
      case h: Header => textOf(h.spans)
    }.headOption.getOrElse { "Untitled" }
}
