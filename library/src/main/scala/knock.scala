package pamflet

import java.io.File
import com.tristanhunt.knockoff._
import collection.immutable.Map

object Knock {
  lazy val discounter = PamfletDiscounter
  def notifyBeginLanguage(): Unit = discounter.notifyBeginLanguage()
  def notifyBeginPage(): Unit = discounter.notifyBeginPage()

  def knockEither(value: String, propFiles: Seq[File], fencePlugins: List[FencePlugin]): Either[Throwable, (String, Seq[Block], Template)] = {
    val frontin = Frontin(value)
    val template = StringTemplate(propFiles.toSeq, frontin header, Map())
    knockEither(frontin, template, fencePlugins)
  }
  def knockEither(frontin: Frontin, template: Template, fencePlugins: List[FencePlugin]): Either[Throwable, (String, Seq[Block], Template)] = {
    val raw = template(frontin body)
    try {
      Right((raw.toString, discounter.knockoffWithPlugins(raw, fencePlugins), template))
    } catch {
      case e: Throwable => Left(e)
    }
  }
}
