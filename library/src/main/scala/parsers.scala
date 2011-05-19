package pamflet
import com.tristanhunt.knockoff._

object PamfletDiscounter extends Discounter {
  override def newChunkParser : ChunkParser = new PamfletChunkParser
}
class PamfletChunkParser extends ChunkParser {
  override def chunk : Parser[ Chunk ] = {
    horizontalRule | leadingStrongTextBlock | leadingEmTextBlock | bulletItem |
    numberedItem | indentedChunk | header | blockquote | linkDefinition |
    fencedChunk |
    textBlockWithBreak | textBlock | emptyLines
  }

  def fencedChunk : Parser[ Chunk ] =
    """```\n""".r ~ rep1(unquotedTextLine) <~ """```\n""".r ^^ {
      case ~(start, lines) => IndentedChunk(foldedString(lines))
    }

  def unquotedTextLine : Parser[ Chunk ] =
    """(?!```)[^\n]+\n""".r ^^ { TextChunk(_) }

  private def foldedString( texts : List[ Chunk ] ) : String =
    ( "" /: texts )( (current, text) => current + text.content )
}
