package pamflet

import java.io.{File,FileInputStream}
import org.antlr.stringtemplate.{StringTemplate => STImpl}

trait Template {
  def apply(input: CharSequence): CharSequence
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
}
