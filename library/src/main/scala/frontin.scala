package pamflet

case class Frontin(header: Option[String], body: String)

object Frontin {
  val separator = "---"

  def separates(str: String): Boolean =
    (str.trim == separator) && (str startsWith separator)
  
  def apply(str: String): Frontin =
    str.linesWithSeparators.toList match {
      case Nil => Frontin(None, "")
      case x :: xs if separates(x) =>
        xs span {!separates(_)} match {
          case (h, b) => Frontin(Some(h.mkString("")),
            if (b.isEmpty) "" else b.tail.mkString(""))
        }
      case _ => Frontin(None, str)
    }
}
