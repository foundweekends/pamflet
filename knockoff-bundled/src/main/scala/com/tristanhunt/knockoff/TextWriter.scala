/* 

The utility of plain text is mostly to get the text without any markup, useful
in cases where you're doing something like pulling the header into a title
field, etc.

**TODO** HTML is currently bypassed, we should parse it and strip out the text.

*/

package com.tristanhunt.knockoff

import java.io.{ StringWriter, Writer }

trait TextWriter {
  
  /** Creates a Group representation of the document. */
  def toText( blocks : Seq[Block] ) : String = {
    implicit val writer = new StringWriter
    blocksToText( blocks )
    writer.toString
  }
  
  def blocksToText( blocks : Seq[Block] )( implicit writer : Writer ) : Unit =
    blocks.foreach( blockToText )
  
  def blockToText( block : Block )( implicit writer : Writer ) : Unit = {
    block match {
      case Paragraph( spans, _ ) => spans.foreach( spanToText )
      case Header( _, spans, _ ) => spans.foreach( spanToText )
      case LinkDefinition( _, _, _, _ ) => {}
      case Blockquote( children, _ ) => children.foreach( blockToText )
      case CodeBlock( text, _ ) => writer.write( text.content )
      case HorizontalRule( _ ) => {}
      case OrderedItem( children, _ ) => children.foreach( blockToText )
      case UnorderedItem( children, _ ) => children.foreach( blockToText )
      case OrderedList( items ) => items.foreach( blockToText )
      case UnorderedList( items ) => items.foreach( blockToText )
    }
    writer.write(" ")
  }
  
  def spanToText( span : Span )( implicit writer : Writer ) : Unit = {
    span match {
      case Text( content ) => writer.write( content )
      case HTMLSpan( html ) => {} 
      case CodeSpan( code ) => writer.write( code )
      case Strong( children ) => children.foreach( spanToText )
      case Emphasis( children ) => children.foreach( spanToText )
      case Link( children, url, title ) => children.foreach( spanToText )
      case IndirectLink( children, definition ) => children.foreach( spanToText )
      case ImageLink( children, url, title ) => children.foreach( spanToText )
      case IndirectImageLink( children, definition ) => children.foreach( spanToText )
    }
    writer.write( " " )
  }
}
