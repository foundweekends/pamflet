package pamflet

sealed trait TocType {
  def css: String
}
object TocType {
  case object Left extends TocType {
    override def css: String = "show" 
  }
  case object Bottom extends TocType {
    override def css: String = "show" 
  }
  case object Hide extends TocType {
    override def css: String = "d-none" 
  }
  case object Collapse extends TocType {
    override def css: String = "collapse" 
  }
}
