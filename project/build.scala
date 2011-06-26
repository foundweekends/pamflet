import sbt._
import Keys._

object PamfletBuild extends Build {
  lazy val common = Defaults.defaultSettings ++ Seq(
    organization := "net.databinder",
    version := "0.2.2",
    crossScalaVersions := Seq("2.8.1"),
    publishTo := Some("Scala Tools Nexus" at 
      "http://nexus.scala-tools.org/content/repositories/releases/"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  lazy val pamflet: Project =
    Project("pamflet", file("."), 
            settings = common) aggregate(app, plugin)
  lazy val knockoff: Project =
    Project("pamflet-knockoff", file("knockoff"),
            settings = common)
  lazy val library: Project =
    Project("pamflet-library", file("library"),
            settings = common) dependsOn knockoff
  lazy val app: Project =
    Project("pamflet-app", file("app"),
            settings = common) dependsOn library
  lazy val plugin: Project =
    Project("pamflet-plugin", file("plugin"),
            settings = common) dependsOn library
}
