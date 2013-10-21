lazy val common = ls.Plugin.lsSettings ++ Seq(
  organization := "net.databinder",
  version := "0.5.0-alpha1",
  scalaVersion := "2.10.3",
  crossScalaVersions := Seq("2.10.3"),
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

val knockoffVersion = "0.8.1"
lazy val knockoffDeps = Def.setting { Seq(
  "com.tristanhunt" %% "knockoff" % knockoffVersion
)}
val unfilteredVersion = "0.7.0"
val stringtemplateVersion = "3.2.1"
lazy val libraryDeps = Def.setting { Seq(
  "net.databinder" %% "unfiltered-filter" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty" % unfilteredVersion,
  "org.antlr" % "stringtemplate" % stringtemplateVersion
)}
val launcherInterfaceVersion = "0.13.0"
val servletApiVersion = "2.5"
lazy val appDeps = Def.setting { Seq(
  "org.scala-sbt" % "launcher-interface" % launcherInterfaceVersion % "provided",
  "javax.servlet" % "servlet-api" % servletApiVersion
)}

lazy val pamflet: Project =
  (project in file(".")).
  settings(common: _*).
  settings(
    name := "pamflet",
    ls.Plugin.LsKeys.skipWrite := true,
    publishArtifact := false
  ).
  aggregate(knockoff, library, app)
lazy val knockoff: Project =
  (project in file("knockoff")).
  settings(common: _*).
  settings(
    name := "pamflet-knockoff",
    description := "Extensions to the Knockoff Markdown parser",
    crossScalaVersions := Seq("2.9.0", "2.9.1", "2.10.3"),
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
  settings(
    name := "pamflet-app",
    description :=
      "Pamflet app for previewing and publishing project documentation",
    libraryDependencies ++= appDeps.value
  ).
  dependsOn(library)
