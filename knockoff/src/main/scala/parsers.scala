package pamflet

import com.tristanhunt.knockoff._
import scala.util.parsing.input.{ CharSequenceReader, Position, Reader }

object PamfletDiscounter extends Discounter with FencedDiscounter

class PamfletChunkParser extends ChunkParser with FencedChunkParser {
  override def chunk : Parser[ Chunk ] = {
    horizontalRule | leadingStrongTextBlock | leadingEmTextBlock | 
    bulletItem | numberedItem | indentedChunk | header | blockquote | 
    linkDefinition | fencedChunk | textBlockWithBreak | textBlock | 
    emptyLines
  }
}
