package pamflet

import knockoff._
import scala.util.parsing.input.Position
import collection.mutable.ListBuffer

trait FencedDiscounter extends Discounter with XHTMLWriter {
  /** List of FencePlugin */
  def fencePlugins: List[FencePlugin]

  override def newChunkParser : ChunkParser =
    new ChunkParser with FencedChunkParser
  override def blockToXHTML: Block => xml.Node = {
    val fallback: PartialFunction[Block, xml.Node] = { case x => super.blockToXHTML(x) }
    val fs: List[PartialFunction[Block, xml.Node]] =
      (fencePlugins map {_.blockToXHTML}) ++ List(FencePlugin.Plain.blockToXHTML, fallback)
    fs.reduceLeft(_ orElse _)
  }

  def notifyBeginLanguage(): Unit =
    fencePlugins foreach {_.onBeginLanguage()}
  def notifyBeginPage(): Unit =
    fencePlugins foreach {_.onBeginPage()}

  def fencedChunkToBlock(language: Option[String], content: String, position: Position,
      list: ListBuffer[Block]): Block = {
    val processors: List[PartialFunction[(Option[String], String, Position, ListBuffer[Block]), Block]] =
      fencePlugins ++ List(FencePlugin.Plain)
    val f = processors.reduceLeft(_ orElse _)
    f((language, content, position, list))
  }
}

trait MutableFencedDiscounter extends FencedDiscounter {
  private[this] val fencePluginBuffer: ListBuffer[FencePlugin] = ListBuffer()
  def registerFencedPlugin(p: FencePlugin): Unit = fencePluginBuffer.append(p)
  def fencePlugins = fencePluginBuffer.toList
  def clearFencePlugins(): Unit = fencePluginBuffer.clear()
  def knockoffWithPlugins(source: java.lang.CharSequence, ps: List[FencePlugin]): collection.Seq[Block] =
    {
      clearFencePlugins()
      ps foreach registerFencedPlugin
      super.knockoff(source)
    }
}

trait FencedChunkParser extends ChunkParser {
  override def chunk : Parser[ Chunk ] = {
    horizontalRule | leadingStrongTextBlock | leadingEmTextBlock | bulletItem |
    numberedItem | indentedChunk | header | blockquote | linkDefinition |
    htmlBlock | fencedChunk | textBlockWithBreak | textBlock | emptyLines | emptySpace
  }
  
  def fencedChunk : Parser[ Chunk ] =
    fence ~> opt(brush) ~ emptyLine ~
      rep1(unquotedTextLine | emptyLine) <~ fence <~ emptyLine ^^ {
        case (brush ~ _) ~ lines =>
          FencedChunk(foldedString(lines), brush.map { _.content })
      }

  def brush : Parser[Chunk] =
    """[ ]*[^\n]+""".r ^^ { b => TextChunk(b.trim) }

  def fence : Parser[Chunk] =
    "```" ^^ { _ => EmptySpace("") }

  def unquotedTextLine : Parser[ Chunk ] =
    """(?!```)[^\n]+\n""".r ^^ { TextChunk(_) }

  private def foldedString( texts : List[ Chunk ] ) : String =
    texts.foldLeft("")( (current, text) => current + text.content )
}

case class FencedChunk(val content: String, language: Option[String])
extends Chunk {
  def appendNewBlock( list : ListBuffer[Block],
                      remaining : List[ (Chunk, collection.Seq[Span], Position) ],
                      spans : collection.Seq[Span], position : Position,
                      discounter : Discounter ): Unit = discounter match {
    case fd: FencedDiscounter => list += fd.fencedChunkToBlock(language, content, position, list)
    case _ => sys.error("Expected FencedDiscounter") 
  }
}

/** A FencePlugin must implement the following methods:
 * 1. def isDefinedAt(language: Option[String]): Boolean
 * 2. def toBlock(language: Option[String], content: String, position: Position, list: ListBuffer[Block]): Block
 * 3. def blockToXHTML: PartialFunction[Block, xml.Node]
 *
 * First, you have to declare what "language" your FencePlugin supports with `isDefinedAt`.
 * Next, in `toBlock` evaluate the incoming content and store them in a custom case class that extends `Block`.
 * Finally, in `blockToXHTML` turn your custom case class into an xml `Node`.
 */
trait FencePlugin extends PartialFunction[(Option[String], String, Position, ListBuffer[Block]), Block] {
  def isDefinedAt(language: Option[String]): Boolean
  def toBlock(language: Option[String], content: String, position: Position, list: ListBuffer[Block]): Block
  def blockToXHTML: PartialFunction[Block, xml.Node]

  override def isDefinedAt(x: (Option[String], String, Position, ListBuffer[Block])): Boolean = isDefinedAt(x._1)
  override def apply(x: (Option[String], String, Position, ListBuffer[Block])): Block = toBlock(x._1, x._2, x._3, x._4)
  def onBeginLanguage(): Unit = ()
  def onBeginPage(): Unit = ()
}
object FencePlugin {
  val Plain: FencePlugin = new FencePlugin {
    override def isDefinedAt(language: Option[String]): Boolean = true
    override def toBlock(language: Option[String], content: String, position: Position, list: ListBuffer[Block]): Block =
      FencedCodeBlock(Text(content), position, language)
    override def blockToXHTML = {
      case FencedCodeBlock(text, _, language) => fencedChunkToXHTML(text, language)
    }
    def fencedChunkToXHTML(text: Text, language: Option[String]) =
      <pre><code class={
        language.map { "prettyprint lang-" + _ }.getOrElse("")
      }>{ text.content }</code></pre>    
  }
}

case class FencedCodeBlock(text: Text, position: Position, 
                           language: Option[String]) extends Block
