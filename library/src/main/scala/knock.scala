package pamflet

import java.io.File
import com.tristanhunt.knockoff._
import collection.immutable.Map

object Knock {
  def knockEither(value: String, propFiles: Seq[File]): Either[Throwable, (Seq[Block], Template)] = {
    val frontin = Frontin(value)
    val template = StringTemplate(propFiles, frontin header, Map())
    try {
      Right(PamfletDiscounter.knockoff(template(frontin body)) -> template)
    } catch {
      case e: Throwable => Left(e)
    }
  }

  def knockEither(value: String, template0: Template): Either[Throwable, (Seq[Block], Template)] = {
    val frontin = Frontin(value)
    val template = frontin.header match {
      case None    => template0
      case Some(h) => template0.updated(h)
    }
    try {
      Right(PamfletDiscounter.knockoff(template(frontin body)) -> template)
    } catch {
      case e: Throwable => Left(e)
    }
  }

}
