import sbt._
object PluginDef extends Build {
  override def projects = Seq(root)
  lazy val root = Project("plugins", file(".")) dependsOn(conscript, gpg)
  lazy val conscript = uri("git://github.com/n8han/conscript-plugin.git#0.3.4")
  lazy val gpg = uri("git://github.com/sbt/xsbt-gpg-plugin.git#0.4") // 0.5 and 0.6 are broken 13.08.2012
}
