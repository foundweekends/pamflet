package pamflet

import com.tristanhunt.knockoff._
import scala.util.parsing.input.{ CharSequenceReader, Position, Reader }

trait SmartyDiscounter extends Discounter {
  override def createSpanConverter(
    linkDefinitions: Seq[LinkDefinitionChunk]) =
    new SpanConverter(linkDefinitions) with SmartySpanConverter
}

trait SmartySpanConverter extends SpanConverter {
  def smartyMatchers : List[ String => Option[SpanMatch] ] = List(
    matchOpenSQuote
  )
  override def matchers =  smartyMatchers ::: super.matchers

  private val matchOpenSQuoteRE = """'(\w+)""".r
  
  def matchOpenSQuote( source : String ) : Option[ SpanMatch ] =
    matchOpenSQuoteRE.findFirstMatchIn( source ).map { qmatch =>
      val before = qmatch.before.toOption.map( Text(_) )
      val html = HTMLSpan( "‘" + qmatch.group(1) )
      SpanMatch( qmatch.start, before, html, qmatch.after.toOption )
    }
}
