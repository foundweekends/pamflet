package pamflet

import java.io.File
import knockoff._
import collection.immutable.Map

object Knock {
  lazy val discounter = PamfletDiscounter
  def notifyBeginLanguage(): Unit = discounter.notifyBeginLanguage()
  def notifyBeginPage(): Unit = discounter.notifyBeginPage()

  def knockEither(value: String, propFiles: collection.Seq[File], ps: List[FencePlugin]): Either[Throwable, (String, collection.Seq[Block], Template)] = 
    knockEither(value, StringTemplate(propFiles, None, Map()), ps)

  def knockEither(value: String, template0: Template): Either[Throwable, (String, collection.Seq[Block], Template)] =
    knockEither(value, template0, List())

  def knockEither(value: String, template0: Template, ps: List[FencePlugin]): Either[Throwable, (String, collection.Seq[Block], Template)] = {
    val frontin = Frontin(value)
    val template = frontin.header match {
      case None    => template0
      case Some(h) => template0.updated(h)
    }
    val raw = template(frontin body)
    try {
      Right((raw.toString, discounter.knockoffWithPlugins(raw, ps), template))
    } catch {
      case e: Throwable => Left(e)
    }
  }

}
