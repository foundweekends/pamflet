import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val pf = "net.databinder" % "pamflet-plugin" % "0.1.0-SNAPSHOT"
}
