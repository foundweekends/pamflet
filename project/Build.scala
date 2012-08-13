import sbt._
import Keys._

object PamfletBuild extends Build {
  lazy val common = Defaults.defaultSettings ++ ls.Plugin.lsSettings ++ Seq(
    organization := "net.databinder",
    version := "0.4.2-SNAPSHOT",
    crossScalaVersions := Seq("2.9.1", "2.9.2"),
    publishTo := Some("Scala Tools Nexus" at 
      "http://nexus.scala-tools.org/content/repositories/releases/"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    homepage :=
      Some(new java.net.URL("http://pamflet.databinder.net/")),
    licenses := Seq("LGPL v3" -> url("http://www.gnu.org/licenses/lgpl.txt")),
    publishMavenStyle := true,
    publishTo :=
      Some("releases" at
           "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
    publishArtifact in Test := false,
    pomExtra := (
      <scm>
        <url>git@github.com:n8han/pamflet.git</url>
        <connection>scm:git:git@github.com:n8han/pamflet.git</connection>
      </scm>
      <developers>
        <developer>
          <id>n8han</id>
          <name>Nathan Hamblen</name>
          <url>http://github.com/n8han</url>
        </developer>
        <developer>
          <id>eed3si9n</id>
          <name>Eugene Yokota</name>
          <url>https://github.com/eed3si9n</url>
        </developer>
      </developers>)
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
    Project(
      "pamflet-app",
      file("app"),
      settings = common ++ conscript.Harness.conscriptSettings ++ Seq(
        description :=
          "Pamflet app for previewing and publishing project documentation"
      )
    ) dependsOn library
}
