package pamflet

import java.io.File
import com.tristanhunt.knockoff._
import collection.immutable.Map

object Knock {
  def knockEither(value: String, propFiles: Seq[File]): Either[Throwable, (String, Seq[Block], Template)] = {
    val frontin = Frontin(value)
    val template = StringTemplate(propFiles.toSeq, frontin header, Map())
    val raw = template(frontin body)
    try {
      Right((raw.toString, PamfletDiscounter.knockoff(raw), template))
    } catch {
      case e: Throwable => Left(e)
    }
  }

  def knockEither(value: String, template0: Template): Either[Throwable, (String, Seq[Block], Template)] = {
    val frontin = Frontin(value)
    val template = frontin.header match {
      case None    => template0
      case Some(h) => template0.updated(h)
    }
    val raw = template(frontin body)
    try {
      Right((raw.toString, PamfletDiscounter.knockoff(raw), template))
    } catch {
      case e: Throwable => Left(e)
    }
  }

}
