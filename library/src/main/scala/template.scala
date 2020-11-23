package pamflet

import java.io.{
  File,FileInputStream,InputStreamReader,StringReader}
import java.nio.charset.Charset
import org.stringtemplate.v4.ST
import collection.immutable.Map
import collection.JavaConverters._
import scala.collection.immutable.Nil

trait Template {
  /** Replace template values in input stream with bound properties */
  def apply(input: CharSequence): CharSequence
  /** Return property for given key if present */
  def get(key: String): Option[String]
  def defaultLanguage: String
  def defaultEncoding: String
  def languages: collection.Seq[String]
  /** Return a new instance of Template with additional pairs. */
  def updated(s: String): Template
  /** Return a new instance of Template with additional pairs. */
  def updated(ext: Map[String, AnyRef]): Template
}

case class StringTemplate(files: collection.Seq[File],
    str: Option[String],
    extra: Map[AnyRef, AnyRef]) extends Template {
  def apply(input: CharSequence) =
    if (!files.isEmpty) {
      val st = new ST(input.toString, '$', '$')
      (properties.asScala ++ extra).foreach {
        case (key, value) =>
          pairToAttribute(key.toString(), value).fold(
            pair => st.add(pair._1, pair._2), pair => st.add(pair._1, pair._2)
          )
      }
      st.render()
    } else input
    
  private def pairToAttribute(key: String, value: Object): Either[(String, Object), (String, Map[String, Object])] = {
    def pairToAttributeMap(first: String, rest: List[String], value: Object): Map[String, Object] = {
        rest match {
          case Nil => Map(first -> value)
          case head :: tail => Map(first -> pairToAttributeMap(head, tail, value))
        }
    }

    key.split('.').toList match {
      case first :: second :: tail => Right(first -> pairToAttributeMap(second, tail, value))
      case _ => Left(key -> value)
    }
  }

  private def properties = {
    val p = new java.util.Properties
    for (f <- files) {
      val q = new java.util.Properties 
      q.load(new InputStreamReader(new FileInputStream(f),
                                   Charset.forName("UTF-8")))
      q.asScala.foreach {
        case (key, value) => p.put(key, value)
      }
    }
    for (s <- str) {
      val q = new java.util.Properties
      q.load(new StringReader(s))
      q.asScala.foreach {
        case (key, value) => p.put(key, value)
      }
    }
    p
  }
  def get(key: String) = Option(properties.get(key)) map { _.toString }
  lazy val defaultLanguage: String =
    get("language") getOrElse "en"
  lazy val defaultEncoding: String = 
    get("inputEncoding") getOrElse Charset.defaultCharset.name
  lazy val languages: collection.Seq[String] =
    get("languages") match {
      case Some(xs) => xs.split(",").toSeq map {_.trim}
      case None     => Seq(defaultLanguage) 
    }
  def updated(ext: Map[String, AnyRef]): Template =
    this.copy(extra = extra ++ ext)
  def updated(s: String): Template = 
    this.copy(str = Some(str.getOrElse("") + "\n" + s))
}
