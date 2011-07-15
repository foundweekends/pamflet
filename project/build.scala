import sbt._
import Keys._

object PamfletBuild extends Build {
  lazy val common = Defaults.defaultSettings ++ Seq(
    organization := "net.databinder",
    version := "0.2.5",
    crossScalaVersions := Seq("2.8.1"),
    publishTo := Some("Scala Tools Nexus" at 
      "http://nexus.scala-tools.org/content/repositories/releases/"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  lazy val pamflet: Project =
    Project("pamflet", file("."), 
            settings = common) aggregate(knockoff, library, app)
  lazy val knockoff: Project =
    Project("pamflet-knockoff", file("knockoff"),
            settings = common)
  lazy val library: Project =
    Project("pamflet-library", file("library"),
            settings = common) dependsOn knockoff
  lazy val app: Project =
    Project("pamflet-app", file("app"),
            settings = common) dependsOn library
}
