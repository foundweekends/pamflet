import sbt._
object PluginDef extends Build {
  override def projects = Seq(root)
  lazy val root = Project("plugins", file(".")) dependsOn(conscript)
  lazy val conscript = uri("git://github.com/n8han/conscript-plugin.git#ff4599")
}
