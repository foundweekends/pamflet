package pamflet

import com.tristanhunt.knockoff._
import scala.util.parsing.input.{ CharSequenceReader, Position, Reader }

object PamfletDiscounter
  extends Discounter 
  with FencedDiscounter
  with SmartyDiscounter
