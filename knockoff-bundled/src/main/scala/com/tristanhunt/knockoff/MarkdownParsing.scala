/*

# Part 2.B. Markdown Parsing #

Parsing is done in three steps:

1. Chunking - The document is converted to a series of Chunk objects, each
eventually mapping to a block. This is kicked off by the `ChunkStreamFactory`. A
`Chunk` is generally a "block-level" element, but the final determination of
what is a block level element isn't complete until step 3.

2. Spanning - The spans of each chunk are identified.

3. Object model creation.

Note that things like block quotes and more complex lists turn into "documents
within documents".

### A Bit Of History To Satisfy Myself

In my first attempt, I tried building one big parser combinator, and then,
slowly, some part of my brain fell down a well. So that's why there's those
steps 2 and 3, it's not because I'm super smart, it's because it got the job
done.


## Chunking ##

Breaks the markdown document into a Stream of `Chunk`s, so that later
recognition can function. This means this

* Identifies the major boundaries of block elments
* Figures out the `LinkDefinition`s. Those are needed for Span recognition.

When we run into something we can't parse, there's a simple rule; go on. If I
detect that there will be more and more problems, well. Hm.

Notably, this remembers the position of each chunk in the input.

### The Chunk Stream Factory

The whole process is wrapped by a "factory" which mostly handles continuing past
any errors in the document if possible. Errors are logged and we move ahead.

*/

package com.tristanhunt.knockoff

import scala.annotation.tailrec
import scala.util.parsing.input.{CharSequenceReader, Position, Reader}

trait ChunkStreamFactory {

  /** Overridable factory method. */
  def newChunkParser: ChunkParser = new ChunkParser

  lazy val chunkParser: ChunkParser = newChunkParser

  def createChunkStream(str: String): Stream[(Chunk, Position)] =
    createChunkStream(new CharSequenceReader(str, 0))

  def createChunkStream(reader: Reader[Char]): Stream[(Chunk, Position)] = {
    if (reader.atEnd) return Stream.empty
    chunkParser.parse(chunkParser.chunk, reader) match {
      case chunkParser.Error(msg, next) => {
        createChunkStream(next)
      }
      case chunkParser.Failure(msg, next) => {
        createChunkStream(next)
      }
      case chunkParser.Success(result, next) => {
        Stream.cons((result, reader.pos), createChunkStream(next))
      }
    }
  }
}

/*

# The Chunk Parsers #

Mostly, this is a series of regular expressions built to find the next chunk in
a markdown document.

*/

import scala.util.parsing.combinator.RegexParsers

class ChunkParser extends RegexParsers with StringExtras {

  override def skipWhitespace = false

  def chunk: Parser[Chunk] = {
    horizontalRule | leadingStrongTextBlock | leadingEmTextBlock | bulletItem |
      numberedItem | indentedChunk | header | blockquote | linkDefinition |
      htmlBlock | textBlockWithBreak | textBlock | emptyLines | emptySpace
  }

  def emptyLines: Parser[Chunk] =
    rep1(emptyLine) ^^ (str => EmptySpace(foldedString(str)))

  def emptyLine: Parser[Chunk] =
    """[\t ]*\r?\n""".r ^^ (str => EmptySpace(str))

  def emptySpace: Parser[Chunk] =
    """[\t ]*""".r ^^ (str => EmptySpace(str))

  def textBlockWithBreak: Parser[Chunk] =
    rep(textLineWithEnd) ~ hardBreakTextLine ^^ {
      case seq ~ break => TextChunk(foldedString(seq) + break.content)
    }

  def textBlock: Parser[Chunk] =
    rep1(textLine) ^^ {
      seq => TextChunk(foldedString(seq))
    }

  /** Match any line up until it ends with a newline. */
  def textLine: Parser[Chunk] =
    """[\t ]*\S[^\n]*\n?""".r ^^ {
      str => TextChunk(str)
    }

  def textLineWithEnd: Parser[Chunk] =
    """[\t ]*\S[^\n]*[^ \n][ ]?\n""".r ^^ {
      str => TextChunk(str)
    }

  def hardBreakTextLine: Parser[Chunk] =
    """[\t ]*\S[^\n]*[ ]{2}\n""".r ^^ {
      s => TextChunk(s)
    }

  def bulletItem: Parser[Chunk] =
    bulletLead ~ rep(trailingLine) ^^ {
      case ~(lead, texts) => BulletLineChunk(foldedString(lead :: texts))
    }

  /** Match a single line that is likely a bullet item. */
  def bulletLead: Parser[Chunk] =
  // """[ ]{0,3}[*\-+](\t|[ ]{0,4})""".r ~> not("[*\\-+]".r) ~> textLine ^^ {
    """[ ]{0,3}[*\-+](\t|[ ]{0,4})""".r ~> textLine ^^ {
      textChunk => BulletLineChunk(textChunk.content)
    }

  /** A special case where an emphasis marker, using an asterix, on the first word
      in a text block doesn't make the block a list item. We'll only catch lines
      here that have an even number of asterixes, because if it's odd, well, you
      probably have an asterix line indicator followed by an emphasis. */
  def leadingEmTextBlock: Parser[Chunk] =
    """[ ]{0,3}\*""".r ~ notEvenAsterixes ~ rep(textLine) ^^ {
      case ~(~(emLine, s), textSeq) => TextChunk(emLine + s + foldedString(textSeq))
    }

  def notEvenAsterixes = new Parser[String] {

    def apply(in: Reader[Char]): ParseResult[String] = {
      val (line, asterixCount, remaining) = readLine(in, new StringBuilder, 0)
      if (asterixCount >= 1 && asterixCount % 2 == 1) return Success(line, remaining)
      else Failure("Odd number of asterixes, skipping.", in)
    }

    def readLine(in: Reader[Char], sb: StringBuilder, count: Int)
    : (String, Int, Reader[Char]) = {
      if (!in.atEnd) sb.append(in.first)
      if (in.atEnd || in.first == '\n') return (sb.toString, count, in.rest)
      if (in.first == '*') readLine(in.rest, sb, count + 1)
      else readLine(in.rest, sb, count)
    }
  }

  /** A special case where an emphasis marker on a word on a text block doesn't
      make the block a list item. */
  def leadingStrongTextBlock: Parser[Chunk] =
    """[ ]{0,3}\*\*[^*\n]+\*\*[^\n]*\n?""".r ~ rep(textLine) ^^ {
      case ~(strLine, textSeq) => TextChunk(strLine + foldedString(textSeq))
    }

  def numberedItem: Parser[Chunk] =
    numberedLead ~ rep(trailingLine) ^^ {
      case ~(lead, texts) => NumberedLineChunk(foldedString(lead :: texts))
    }

  def numberedLead: Parser[Chunk] =
    """[ ]{0,3}\d+\.(\t|[ ]{0,4})""".r ~> textLine ^^ {
      textChunk => NumberedLineChunk(textChunk.content)
    }

  def trailingLine: Parser[Chunk] =
    """\t|[ ]{0,4}""".r ~> """[\S&&[^*\-+]&&[^\d]][^\n]*\n?""".r ^^ (
      s => TextChunk(s))

  def header: Parser[Chunk] =
    (setextHeaderEquals | setextHeaderDashes | atxHeader)

  def setextHeaderEquals: Parser[Chunk] =
    textLine <~ equalsLine ^^ (s => HeaderChunk(1, s.content.trim))

  def setextHeaderDashes: Parser[Chunk] =
    textLine <~ dashesLine ^^ (s => HeaderChunk(2, s.content.trim))

  def equalsLine: Parser[Any] = """=+\n""".r

  def dashesLine: Parser[Any] = """-+\n""".r

  def atxHeader: Parser[Chunk] =
    """#+ .*\n?""".r ^^ (
      s => HeaderChunk(s.countLeading('#'), s.trimChars('#').trim))

  def horizontalRule: Parser[Chunk] =
    """[ ]{0,3}[*\-_][\t ]?[*\-_][\t ]?[*\-_][\t *\-_]*\n""".r ^^ {
      s => HorizontalRuleChunk
    }

  def indentedChunk: Parser[Chunk] =
    rep1(indentedLine) ^^ (lines => IndentedChunk(foldedString(lines)))

  def indentedLine: Parser[Chunk] =
    """\t|[ ]{4}""".r ~> (textLine | emptyLine | emptyString)

  def emptyString: Parser[Chunk] = "".r ^^ (s => EmptySpace(s))

  def blockquote: Parser[Chunk] =
    blockquotedLine ~ rep(blockquotedLine | textLine) ^^ {
      case ~(lead, trailing) =>
        BlockquotedChunk(foldedString(lead :: trailing))
    }

  def blockquotedLine: Parser[Chunk] =
    """^>[\t ]?""".r ~> (textLine | emptyLine)

  def linkDefinition: Parser[Chunk] =
    linkIDAndURL ~ opt(linkTitle) <~ """[ ]*\n?""".r ^^ {
      case ~(idAndURL, titleOpt) =>
        LinkDefinitionChunk(idAndURL._1, idAndURL._2, titleOpt)
    }

  def htmlBlock = new Parser[Chunk] {

    def apply(in: Reader[Char]): ParseResult[Chunk] = {
      findStart(in, new StringBuilder) match {
        case Some((tagName, sb, rest)) =>
          findEnd(rest, tagName, 1, sb, new StringBuilder) match {

            case Some((text, rest)) =>
              Success(HTMLChunk(text), rest)

            case None =>
              Failure("No end tag found for " + tagName, in)
          }

        case None =>
          Failure("No HTML start tag found", in)
      }
    }

    private val startElement = """^<[ ]*([a-zA-Z0-9:_]+)[ \t]*[^>]*?(/?+)>""".r

    def findStart(in: Reader[Char], sb: StringBuilder): Option[(String, StringBuilder, Reader[Char])] = {
      if (!in.atEnd) sb.append(in.first)
      if (in.atEnd || in.first == '\n') return None
      startElement.findFirstMatchIn(sb.toString).foreach {
        matcher =>
          return Some((matcher.group(1), sb, in.rest))
      }
      findStart(in.rest, sb)
    }

    @tailrec
    def findEnd(in: Reader[Char], tagName: String, openCount: Int,
                sb: StringBuilder, buf: StringBuilder): Option[(String, Reader[Char])] = {
      if (!in.atEnd) {
        sb.append(in.first)
        buf.append(in.first)
      }
      if (in.atEnd) return None
      var openCountArg = openCount
      var bufArg = buf
      ("(?i)<[ ]*" + tagName + "[ ]*[^>]*>").r.findFirstMatchIn(buf.toString) match {
        case Some(matcher) ⇒
          openCountArg = openCount + 1
          bufArg = new StringBuilder
        case None ⇒
          ("(?i)</[ ]*" + tagName + "[ ]*>").r.findFirstMatchIn(buf.toString) match {
            case Some(matcher) if openCount == 1 ⇒
              return Some((sb.toString, in.rest))
            case Some(matcher) ⇒
              openCountArg = openCount - 1
              bufArg = new StringBuilder
            case None ⇒
          }
      }
      findEnd(in.rest, tagName, openCountArg, sb, bufArg)
    }
  }

  private def linkIDAndURL: Parser[(String, String)] =
    """[ ]{0,3}\[[^\[\]]*\]:[ ]+<?[\w\p{Punct}]+>?""".r ^^ {
      linkString =>
        val linkMatch = """^\[([^\[\]]+)\]:[ ]+<?([\w\p{Punct}]+)>?$""".r
          .findFirstMatchIn(linkString.trim).get;
        (linkMatch.group(1), linkMatch.group(2))
    }

  private def linkTitle: Parser[String] =
    """\s*""".r ~> """["'(].*["')]""".r ^^ (// " <- My TextMate bundle fails here
      str => str.substring(1, str.length - 1))

  // Utility Methods

  /** Take a series of very similar chunks and group them. */
  private def foldedString(texts: List[Chunk]): String =
    ("" /: texts)((current, text) => current + text.content)
}

/*


### The Chunks

Chunks are used to capture the major blocks in the early stage, and then once
we've grabbed the spanning elements of each block, to construct the final
`Block` model.

*/

import scala.collection.mutable.ListBuffer

trait Chunk {
  def content: String

  def isLinkDefinition = false

  /** Create the Block and append to the list. */
  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter)
}

case class HTMLChunk(content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    list += HTMLBlock(content, position)
  }
}

/*

### Blockquoted Chunk

Represents a single level of blockquoted material. That means that it could also
contain content, which is then reparsed, recursively.

*/

case class BlockquotedChunk(content: String) extends Chunk {

  /** @param content The material, not parsed, but also not containing this
                     level's '>' characters. */
  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    val blocks = discounter.knockoff(content)
    list += Blockquote(blocks, position)
  }
}

/*

### Empty Space Chunk

Empty space only matters in cases where the lines are indented, which is a way
of dealing with editors that like to do things like strip out whitespace at the
end of a line.

This does not cover forced line brakes.

*/

case class EmptySpace(val content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    if (remaining.isEmpty) return
    if (list.isEmpty) return
    list.last match {
      case lastCB: CodeBlock =>
        remaining.head._1 match {
          case ice: IndentedChunk =>
            list.update(list.length - 1,
              CodeBlock(Text(lastCB.text.content + "\n"),
                lastCB.position))
          case _ => {}
        }
      case _ => {}
    }
  }
}

case class HeaderChunk(val level: Int, val content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    list += Header(level, spans, position)
  }
}

case object HorizontalRuleChunk extends Chunk {
  val content = "* * *\n"

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    list += HorizontalRule(position)
  }
}

/*

### Indented Chunk

This represents a group of lines that have at least 4 spaces or 1 tab preceding
the line.

If the block before is a list, we append this to the end of that list.
Otherwise, append it as a new code block. Two code blocks will get combined here
(because it's common to have an empty line not be indented in many editors).
Appending to the end of a list means that we strip out the first indent and
reparse things.

*/

case class IndentedChunk(val content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    if (list.isEmpty) {
      spans.head match {
        case text: Text => list += CodeBlock(text, position)
      }
    } else {
      list.last match {
        case OrderedList(items) =>
          val blocks = discounter.knockoff(content)
          val li = OrderedItem(items.last.children ++ blocks, items.last.position)
          list.update(list.length - 1, OrderedList(items.take(items.length - 1) ++ List(li)))

        case UnorderedList(items) =>
          val blocks = discounter.knockoff(content)
          val li =
            UnorderedItem(items.last.children ++ blocks, items.last.position)
          list.update(list.length - 1, UnorderedList(items.take(items.length - 1) ++ List(li)))

        case CodeBlock(text, position) =>
          spans.head match {
            case next: Text =>
              list.update(list.length - 1,
                CodeBlock(Text(text.content + next.content), position))
          }

        case _ =>
          spans.head match {
            case text: Text => list += CodeBlock(text, position)
          }
      }
    }
  }
}

case class LinkDefinitionChunk(val id: String, val url: String,
                               val title: Option[String])
  extends Chunk {

  override def isLinkDefinition = true

  def content: String =
    "[" + id + "]: " + url + title.map(" \"" + _ + "\"").getOrElse("")

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    list += LinkDefinition(id, url, title, position)
  }
}

case class NumberedLineChunk(val content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    val li = OrderedItem(List(Paragraph(spans, position)), position)
    if (list.isEmpty) {
      list += OrderedList(List(li))
    } else {
      list.last match {
        case ol: OrderedList =>
          list.update(list.length - 1, OrderedList(ol.items ++ List(li)))
        case _ => list += OrderedList(List(li))
      }
    }
  }
}

/*

### Text Chunk

Here is where I can apply hard breaks in the middle of paragraphs. If we've
recognized a `Text` span that contains two spaces and a newline, we split the
span sequence at this point into two lists, and then append two blocks. One of
them will be an `HTMLSpan(<br/>)`

*/

case class TextChunk(val content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    def appendList = list += Paragraph(spans, position)

    if (list.isEmpty) {
      appendList
    } else {
      list.last match {
        case p: Paragraph =>
          if (endsWithBreak(p.spans)) {
            list.trimEnd(1)
            list += appendBreakAndSpans(p.spans, spans, position)
          }
          else appendList

        case _ => appendList
      }
    }
  }

  def endsWithBreak(spans: Seq[Span]): Boolean = {
    if (spans.isEmpty) return false
    spans.last match {
      case text: Text =>
        text.content.endsWith("  \n")
      case _ => false
    }
  }

  def appendBreakAndSpans(preSpans: Seq[Span], tailSpans: Seq[Span],
                          position: Position): Paragraph = {
    Paragraph(preSpans ++ List(HTMLSpan("<br/>\n")) ++ tailSpans, position)
  }
}

case class BulletLineChunk(val content: String) extends Chunk {

  def appendNewBlock(list: ListBuffer[Block],
                     remaining: List[(Chunk, Seq[Span], Position)],
                     spans: Seq[Span], position: Position,
                     discounter: Discounter) {
    val li = UnorderedItem(List(Paragraph(spans, position)), position)
    if (list.isEmpty) {
      list += UnorderedList(List(li))
    } else {
      list.last match {
        case ul: UnorderedList =>
          list.update(list.length - 1, UnorderedList(ul.items ++ List(li)))
        case _ => list += UnorderedList(List(li))
      }
    }
  }
}

/*

## Spanning ##

Our little spanning matching system is broken up into a tail-recursive system
that slowly puts together our strings by:

1. Trying out all alternatives of the next significant spanning element from the
current point.

2. Picking the best match based on earliest first location.

3. Processing current content if it can.

4. Processing the rest of the tail content.

### Span Converter

The converter implements the tail-recursive methods for spinning through the
content. Note that this recurses in two directions. One, when we find the next
spanning element, this will call itself to work on the tail, iterating "down"
the string. But on certain elements, the element itself contains a Span, so this
converter configures that matcher to kick off another parsing run on the
substring of that span.

*/


class SpanConverter(definitions: Seq[LinkDefinitionChunk])
  extends Function1[Chunk, Seq[Span]] with StringExtras {

  /*
    The primary result returned by a `SpanMatcher`. It's `index` will become an
    ordering attribute for determining the "best" match.
  */
  case class SpanMatch(index: Int, before: Option[Text], current: Span,
                       after: Option[String])


  /** @param delim The delimiter string to match the next 2 sequences of.
  @param toSpanMatch Factory to create the actual SpanMatch.
  @param recursive If you want the contained element to be reconverted.
  @param escape If set, how you can escape this sequence. */
  class DelimMatcher(delim: String, toSpan: Seq[Span] => Span,
                     recursive: Boolean, escape: Option[Char])
    extends Function1[String, Option[SpanMatch]] {

    def apply(source: String): Option[SpanMatch] = {

      source.nextNIndicesOf(2, delim, escape) match {
        case List(start, end) =>
          if (start + delim.length >= end) return None
          val contained = source.substring(start + delim.length, end)
          val content = if (recursive) convert(contained, Nil)
          else List(Text(contained))
          val before = source.substringOption(0, start).map(Text(_))
          val after = source.substringOption(end + delim.length, source.length)
          val mapped = toSpan(content)
          Some(SpanMatch(start, before, mapped, after))
        case _ => None
      }
    }
  }

  def apply(chunk: Chunk): Seq[Span] = {
    chunk match {
      case IndentedChunk(content) => List(new Text(content))
      case _ => convert(chunk.content, Nil)
    }
  }

  /** Tail-recursive method halts when the content argument is empty. */
  protected def convert(content: String, current: List[Span]): Seq[Span] = {

    if (content.isEmpty) return current

    val textOnly = SpanMatch(content.length, None, Text(content), None)

    val best = (textOnly /: matchers) {
      (current, findMatch) =>
        findMatch(content) match {
          case None => current
          case Some(nextMatch) =>
            if (nextMatch.index < current.index) nextMatch
            else current
        }
    }

    val updated = current ::: best.before.toList ::: List(best.current)

    best.after match {
      case None => updated
      case Some(remaining) => convert(remaining, updated)
    }
  }

  def matchers: List[String => Option[SpanMatch]] = List(
    matchDoubleCodes, matchSingleCodes, findReferenceMatch, findAutomaticMatch,
    findNormalMatch, matchHTMLComment,
    matchEntity, matchHTMLSpan, matchUnderscoreStrongAndEm,
    matchAsterixStrongAndEm, matchUnderscoreStrong, matchAsterixStrong,
    matchUnderscoreEmphasis, matchAsterixEmphasis
  )

  val matchUnderscoreEmphasis =
    new DelimMatcher("_", Emphasis(_), true, Some('\\'))

  val matchAsterixEmphasis =
    new DelimMatcher("*", Emphasis(_), true, Some('\\'))


  val matchUnderscoreStrong =
    new DelimMatcher("__", Strong(_), true, Some('\\'))

  val matchAsterixStrong =
    new DelimMatcher("**", Strong(_), true, Some('\\'))


  val matchUnderscoreStrongAndEm =
    new DelimMatcher("___", seq => Strong(List(Emphasis(seq))), true,
      Some('\\'))

  val matchAsterixStrongAndEm =
    new DelimMatcher("***", seq => Strong(List(Emphasis(seq))), true,
      Some('\\'))

  val matchDoubleCodes =
    new DelimMatcher("``", s => CodeSpan(s.head.asInstanceOf[Text].content),
      false, None)

  val matchSingleCodes =
    new DelimMatcher("`", s => CodeSpan(s.head.asInstanceOf[Text].content),
      false, None)


  /*

    ### HTML Matching

    If we find any kind of HTML/XML-like element within the content, and it's
    not a single element, we try to find the ending element. If that segment
    isn't well-formed, we just ignore the element, and treat it like text.

    Any sequences of HTML in content are matched by the `InlineHTMLMatcher`.
    Note that this uses a recursive method `hasMatchedClose` to deal with the
    situations where one span contains other spans - it's basically like
    parenthesis matching.

  */

  private val startElement = """<[ ]*([a-zA-Z0-9:_]+)[ \t]*[^>]*?(/?+)>""".r

  def matchHTMLSpan(source: String): Option[SpanMatch] = {
    startElement.findFirstMatchIn(source).map {
      open =>
        val hasEnd = open.group(2) == "/"
        val before = open.before.toOption.map(Text(_))
        val noEnd = SpanMatch(open.start, before, HTMLSpan(open.matched),
          open.after.toOption)
        if (!hasEnd) {
          hasMatchedClose(source, open.group(1), open.end, 1) match {
            case Some((close, after)) =>
              val before = open.before.toOption.map(Text(_))
              val html = HTMLSpan(source.substring(open.start, close))
              SpanMatch(open.start, before, html, after.toOption)
            // Let no html-like thing go unpunished.
            case None => noEnd
          }
        } else {
          noEnd
        }
    }
  }

  private def hasMatchedClose(source: String, tag: String, from: Int,
                              opens: Int)
  : Option[(Int, CharSequence)] = {

    val opener = ("(?i)<[ ]*" + tag + "[ \t]*[^>]*?(/?+)*>").r
    val closer = ("(?i)</[ ]*" + tag + "[ ]*>").r

    val nextOpen = opener.findFirstMatchIn(source.substring(from))
    val nextClose = closer.findFirstMatchIn(source.substring(from))

    if (!nextClose.isDefined) return None

    if (nextOpen.isDefined && (nextOpen.get.start < nextClose.get.start)) {
      hasMatchedClose(source, tag, from + nextOpen.get.end, opens + 1)
    } else if (opens > 1) {
      hasMatchedClose(source, tag, from + nextClose.get.end, opens - 1)
    } else {
      Some((from + nextClose.get.end, nextClose.get.after))
    }
  }

  private val matchEntityRE = """&\w+;""".r

  def matchEntity(source: String): Option[SpanMatch] =
    matchEntityRE.findFirstMatchIn(source).map {
      entityMatch =>
        val before = entityMatch.before.toOption.map(Text(_))
        val html = HTMLSpan(entityMatch.matched)
        SpanMatch(entityMatch.start, before, html, entityMatch.after.toOption)
    }

  def matchHTMLComment(source: String): Option[SpanMatch] = {
    val open = source.indexOf("<!--")
    if (open > -1) {
      val close = source.indexOf("-->", open)
      if (close > -1) {
        val before = source.substring(0, open).toOption.map(Text(_))
        val html = HTMLSpan(source.substring(open, close + "-->".length))
        val after = source.substring(close + "-->".length).toOption
        return Some(SpanMatch(open, before, html, after))
      }
    }
    return None
  }


  private val automaticLinkRE = """<((http:|mailto:|https:)\S+)>""".r

  def findAutomaticMatch(source: String): Option[SpanMatch] =
    automaticLinkRE.findFirstMatchIn(source).map {
      aMatch =>
        val url = aMatch.group(1)
        val before = aMatch.before.toOption.map(Text(_))
        val link = Link(List(Text(url)), url, None)
        SpanMatch(aMatch.start, before, link, aMatch.after.toOption)
    }

  // Finds links in the format [name](link) for normal links or ![name](link)
  // for images.
  def findNormalMatch(source: String): Option[SpanMatch] = {
    var imageIdx = source.indexOf('!')

    val firstOpen = source.indexOf('[')
    if (firstOpen == -1) return None

    val firstClose =
      source.findBalanced('[', ']', firstOpen).getOrElse(return None)

    val wrapped = source.substring(firstOpen + 1, firstClose)

    val secondPart = source.substring(firstClose + 1)

    val secondMatch = """^\s*(\()""".r.findFirstMatchIn(secondPart).getOrElse(return None)

    val secondOpen = secondMatch.start(1)

    var secondClose =
      secondPart.findBalanced('(', ')', secondOpen).get

    if (secondClose == -1) return None

    var titleMatcher = """<?([\S&&[^)>]]*)>?[\t ]+"([^)]*)"""".r // "

    var linkContent = secondPart.substring(secondOpen + 1, secondClose)

    var titleOpt: Option[String] = None
    var url: String = ""

    titleMatcher.findFirstMatchIn(linkContent) match {
      case Some(matcher) =>
        url = matcher.group(1)
        titleOpt = Some(matcher.group(2))

      case None =>
        url = linkContent
        titleOpt = None
    }

    """<(.*)>""".r.findFirstMatchIn(url).foreach(x => url = x.group(1))

    val link = if (imageIdx < firstOpen && imageIdx != -1)
      ImageLink(convert(wrapped, Nil), url, titleOpt)
    else
      Link(convert(wrapped, Nil), url, titleOpt)

    val start = if (imageIdx != -1) Math.min(imageIdx, firstOpen)
    else firstOpen

    val beforeOpt = if (start > 0) Some(Text(source.substring(0, start)))
    else None

    val close = firstClose + secondClose + 1

    val afterOpt = if (source.length > close + 1)
      Some(source.substring(close + 1))
    else None

    Some(SpanMatch(start, beforeOpt, link, afterOpt))
  }

  /** We have to match parens, to support this stuff: [wr [app] ed] [thing] */
  def findReferenceMatch(source: String): Option[SpanMatch] = {
    val firstOpen = source.indexOf('[')
    if (firstOpen == -1) return None

    val firstClose =
      source.findBalanced('[', ']', firstOpen).getOrElse(return None)

    val secondPart = source.substring(firstClose + 1)

    val secondMatch =
      """^\s*(\[)""".r.findFirstMatchIn(secondPart).getOrElse(return None)

    val secondClose =
      secondPart.findBalanced('[', ']', secondMatch.start(1)).get
    if (secondClose == -1) return None

    val refID = {
      val no2 = secondPart.substring(secondMatch.start(1) + 1, secondClose)
      if (no2.isEmpty) source.substring(firstOpen + 1, firstClose) else no2
    }
    val precedingText = source.substring(0, firstOpen).toOption.map(Text(_))

    definitions.find(_.id equalsIgnoreCase refID).map {
      definition: LinkDefinitionChunk =>
        val link = Link(List(Text(source.substring(firstOpen + 1, firstClose))),
          definition.url, definition.title)
        val after = source.substring(firstClose + secondClose + 2).toOption
        SpanMatch(firstOpen, precedingText, link, after)
    }
  }
}
