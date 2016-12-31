package pamflet

import scala.xml.Node
import knockoff._

// see http://www.w3.org/html/wg/drafts/html/master/syntax.html#void-elements
trait Html5Imgs extends Discounter { self: XHTMLWriter =>
  override def imageLinkToXHTML : ( Seq[Span], String, Option[String] ) => Node = {
    ( spans, url, title ) => <img src={ url } title={ title.getOrElse(null) }
                                  alt={ spans.map( spanToXHTML(_) ) } />
  }
}
