lazy val common = ls.Plugin.lsSettings ++ Seq(
  organization := "net.databinder",
  version := "0.4.3-SNAPSHOT",
  scalaVersion := "2.9.2",
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

val knockoffVersion = "0.8.0-16"
lazy val knockoffDeps = Def.setting { Seq(
  "com.tristanhunt" %% "knockoff" % knockoffVersion
)}
val unfilteredVersion = "0.6.3"
val stringtemplateVersion = "3.2.1"
lazy val libraryDeps = Def.setting { Seq(
  "net.databinder" %% "unfiltered-filter" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty" % unfilteredVersion,
  "org.antlr" % "stringtemplate" % stringtemplateVersion
)}

lazy val pamflet: Project =
  (project in file(".")).
  settings(common: _*).
  settings(
    name := "pamflet",
    ls.Plugin.LsKeys.skipWrite := true
  ).
  aggregate(knockoff, library, app)
lazy val knockoff: Project =
  (project in file("knockoff")).
  settings(common: _*).
  settings(
    name := "pamflet-knockoff",
    description := "Extensions to the Knockoff Markdown parser",
    crossScalaVersions := Seq("2.8.1", "2.9.0", "2.9.1"),
    libraryDependencies ++= knockoffDeps.value
  )
lazy val library: Project =
  (project in file("library")).
  settings(common: _*).
  settings(
    name := "pamflet-library",
    description := "Core Pamflet library",
    libraryDependencies ++= libraryDeps.value
  ).
  dependsOn(knockoff)
lazy val app: Project =
  (project in file("app")).
  settings(common: _*).
  settings(conscript.Harness.conscriptSettings: _*).
  settings(
    name := "pamflet-app",
    description :=
      "Pamflet app for previewing and publishing project documentation"    
  ).
  dependsOn(library)
