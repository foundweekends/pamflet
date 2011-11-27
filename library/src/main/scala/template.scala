package pamflet

import java.io.{File,FileInputStream}
import org.antlr.stringtemplate.{StringTemplate => STImpl}

trait Template {
  /** Replace template values in input stream with bound properties */
  def apply(input: CharSequence): CharSequence
  /** Return property for given key if present */
  def get(key: String): Option[String]
}

case class StringTemplate(file: File) extends Template {
  def apply(input: CharSequence) =
    if (file.exists) {
      val st = new STImpl
      st.setTemplate(input.toString)
      st.setAttributes(properties)
      st.toString
    } else input
    
  private def properties = {
    val p = new java.util.Properties
    p.load(new FileInputStream(file))
    p
  }
  def get(key: String) = Option(properties.get(key)) map { _.toString }
}
