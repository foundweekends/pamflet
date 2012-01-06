import sbt._
import Keys._

object PamfletBuild extends Build {
  lazy val common = Defaults.defaultSettings ++ ls.Plugin.lsSettings ++ Seq(
    organization := "net.databinder",
    version := "0.3.1",
    scalaVersion := "2.9.1",
    publishTo := Some("Scala Tools Nexus" at 
      "http://nexus.scala-tools.org/content/repositories/releases/"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    homepage :=
      Some(new java.net.URL("http://pamflet.databinder.net/"))

  )

  lazy val pamflet: Project =
    Project("pamflet", file("."), 
            settings = common ++ Seq(
              ls.Plugin.LsKeys.skipWrite := true
            )) aggregate(knockoff, library, app)
  lazy val knockoff: Project =
    Project("pamflet-knockoff", file("knockoff"),
            settings = common ++ Seq(
              description := "Extensions to the Knockoff Markdown parser"
            ))
  lazy val library: Project =
    Project("pamflet-library", file("library"),
            settings = common ++ Seq(
              description := "Core Pamflet library"
            )) dependsOn knockoff
  lazy val app: Project =
    Project("pamflet-app", file("app"),
            settings = common ++ conscript.Harness.conscriptSettings ++ Seq(
              description := "Pamflet app for previewing and publishing"
            )
    ) dependsOn library
}
