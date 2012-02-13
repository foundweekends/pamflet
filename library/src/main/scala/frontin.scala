package pamflet

case class Frontin(header: Option[String], body: String)

object Frontin {
  val seperator = "---"

  def seperates(str: String): Boolean =
    (str.trim == seperator) && (str startsWith seperator)
  
  def apply(str: String): Frontin =
    str.lines.toList match {
      case Nil => Frontin(None, "")
      case x :: xs if seperates(x) =>
        xs span {!seperates(_)} match {
          case (h, b) => Frontin(Some(h.mkString("\n")),
            if (b isEmpty) "" else b.tail.mkString("\n"))
        }
      case _ => Frontin(None, str)
    }
}
