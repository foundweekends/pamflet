package pamflet

import java.io.{File,FileInputStream,ByteArrayInputStream}
import java.nio.charset.Charset
import org.antlr.stringtemplate.{StringTemplate => STImpl}

trait Template {
  /** Replace template values in input stream with bound properties */
  def apply(input: CharSequence): CharSequence
  /** Return property for given key if present */
  def get(key: String): Option[String]
}

case class StringTemplate(file: Option[File], str: Option[String]) extends Template {
  def apply(input: CharSequence) =
    if (file.isDefined) {
      val st = new STImpl
      st.setTemplate(input.toString)
      st.setAttributes(properties)
      st.toString
    } else input
    
  private def properties = {
    val p = new java.util.Properties
    file foreach { f => p.load(new FileInputStream(f)) }
    str foreach { s =>
      val q = new java.util.Properties
      val latin1 = s getBytes Charset.forName("ISO-8859-1")
      q.load(new ByteArrayInputStream(latin1))
      p putAll q
    }
    p
  }
  def get(key: String) = Option(properties.get(key)) map { _.toString }
}
