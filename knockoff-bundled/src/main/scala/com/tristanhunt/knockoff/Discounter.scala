/*

## Converting Markdown to HTML ##

The `Discounter` is what grabs the "knockoff" of a markdown string. For simple usage,
you can use the `DefaultDiscounter` object.

    import com.tristanhunt.knockoff.DefaultDiscounter._
    toXHTML( knockoff( markdownString ) )

*/
package com.tristanhunt.knockoff

import scala.collection.mutable.ListBuffer
import scala.util.parsing.input.Position
import scala.util.parsing.input.CharSequenceReader

trait Discounter extends ChunkStreamFactory with XHTMLWriter with TextWriter {

  /** Parses and returns our best guess at the sequence of blocks. It will
      never fail, just log all suspicious things. */
  def knockoff(source: java.lang.CharSequence): Seq[Block] = {

    val chunks = createChunkStream(new CharSequenceReader(source, 0))

    // These next lines are really ugly because I couldn't figure out a nice
    // way to match a tuple argument (thank you erasure!)
    val linkDefinitions = chunks.flatMap {
      case ((chunk, pos)) =>
        if (chunk.isLinkDefinition)
          List(chunk.asInstanceOf[LinkDefinitionChunk])
        else Nil
    }

    val convert = createSpanConverter(linkDefinitions)

    val spanned = chunks.map {
      chunkAndPos =>
        (chunkAndPos._1, convert(chunkAndPos._1), chunkAndPos._2)
    }

    combine(spanned.toList, new ListBuffer)
  }

  def createSpanConverter(linkDefinitions: Seq[LinkDefinitionChunk]): SpanConverter =
    new SpanConverter(linkDefinitions)

  /** Consume input and append the right thing to the output until empty. The
      Chunk itself determines the "right thing to do". All chunks only know what
      has come before itself, by peering into the output. (It shouldn't matter
      what comes next...) */
  private def combine(input: List[(Chunk, Seq[Span], Position)],
                      output: ListBuffer[Block])
  : Seq[Block] = {
    if (input.isEmpty) return output
    input.head._1.appendNewBlock(output, input.tail, input.head._2,
      input.head._3, this)
    combine(input.tail, output)
  }
}

/*

### The Default Discounter

Provides an object along with a main method for the "good enough to script" kind of
usage. Note that a major aim of this discounter is to mimic the usage of
`Markdown.pl`.

    Markdown.pl [ −−html4tags ] [ −−version ] [ −shortversion ] [ file ... ]

The `--html4tags` argument will just do nothing, but not be processed as a file.


*/

import java.io.File

object DefaultDiscounter extends Discounter {
  def main(args: Array[String]): Unit = try {
    if (args.contains("--version")) {
      Console.err.print("DefaultDiscounter ")
    }
    if (args.contains("--version") || args.contains("-shortversion")) {
      Console.err.println("0.7.1-SNAPSHOT")
      return
    }

    if (args.isEmpty) {
      val sb = new StringBuilder
      var line: String = null
      do {
        line = Console.readLine
        if (line != null) sb.append(line)
      } while (line != null)
      println(toXHTML(knockoff(sb.toString)).toString)
    } else {
      args.filter(_ != "--html4tags").foreach {
        fileName =>
          println(toXHTML(knockoff(readText(fileName))).toString)
      }
    }
  } catch {
    case th: Throwable => {
      th.printStackTrace(Console.err)
    }
  }

  private def readText(fileName: String): String =
    io.Source.fromFile(new File(fileName)).mkString("")
}
