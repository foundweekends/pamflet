package pamflet

import com.tristanhunt.knockoff._

object PamfletDiscounter
  extends Discounter 
  with MutableFencedDiscounter
  with SmartyDiscounter
  with IdentifiedHeaders
  with Html5Imgs
