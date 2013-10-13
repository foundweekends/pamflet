package pamflet

import java.io.{
  File,FileInputStream,ByteArrayInputStream,InputStreamReader,StringReader}
import java.nio.charset.Charset
import org.antlr.stringtemplate.{StringTemplate => STImpl}

trait Template {
  /** Replace template values in input stream with bound properties */
  def apply(input: CharSequence): CharSequence
  /** Return property for given key if present */
  def get(key: String): Option[String]
  def defaultLanguage: String
  def languages: Seq[String]
}

case class StringTemplate(files: Seq[File], str: Option[String]) extends Template {
  def apply(input: CharSequence) =
    if (!files.isEmpty) {
      val st = new STImpl
      st.setTemplate(input.toString)
      st.setAttributes(properties)
      st.toString
    } else input
    
  private def properties = {
    val p = new java.util.Properties
    for (f <- files) {
      val q = new java.util.Properties 
      q.load(new InputStreamReader(new FileInputStream(f),
                                   Charset.forName("UTF-8")))
      p.putAll(q)
    }
    for (s <- str) {
      val q = new java.util.Properties
      q.load(new StringReader(s))
      p.putAll(q)
    }
    p
  }
  def get(key: String) = Option(properties.get(key)) map { _.toString }
  lazy val defaultLanguage: String =
    get("language") getOrElse "en"
  lazy val languages: Seq[String] =
    get("languages") match {
      case Some(xs) => xs.split(",").toSeq map {_.trim}
      case None     => Seq(defaultLanguage) 
    }
}
