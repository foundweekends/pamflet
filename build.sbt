import sbtrelease.ReleaseStateTransformations._

val unusedWarnings = Seq(
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

val Scala212 = "2.12.1"

lazy val common = Seq(
  organization := "org.foundweekends",
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala212, "2.11.8", "2.10.6"),
  scalacOptions ++= Seq("-language:_", "-deprecation", "-Xfuture", "-Yno-adapted-args"),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 11 => unusedWarnings
      case _ => Nil
    }
  },
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  homepage :=
    Some(new java.net.URL("http://www.foundweekends.org/pamflet/")),
  licenses := Seq("LGPL v3" -> url("http://www.gnu.org/licenses/lgpl.txt")),
  publishMavenStyle := true,
  publishTo :=
    Some("releases" at
         "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
  publishArtifact in Test := false,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
    pushChanges
  ),
  pomExtra := (
    <scm>
      <url>git@github.com:foundweekends/pamflet.git</url>
      <connection>scm:git:git@github.com:foundweekends/pamflet.git</connection>
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
) ++ Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

lazy val knockoffDeps = Def.setting { Seq(
  "org.foundweekends" %% "knockoff" % "0.8.6"
)}
val unfilteredVersion = "0.9.0-beta2"
val stringtemplateVersion = "3.2.1"
lazy val libraryDeps = Def.setting { Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.antlr" % "stringtemplate" % stringtemplateVersion
)}
val launcherInterfaceVersion = "0.13.0"
val servletApiVersion = "3.1.0"
lazy val appDeps = Def.setting { Seq(
  "org.scala-sbt" % "launcher-interface" % launcherInterfaceVersion % "provided",
  "javax.servlet" % "javax.servlet-api" % servletApiVersion
)}

lazy val pamflet: Project =
  (project in file(".")).
  settings(common: _*).
  settings(
    ghpages.settings,
    includeFilter in Pamflet := {
      new FileFilter{
        override def accept(file: File) = {
          !file.getCanonicalPath.contains("offline/")
        }
      }
    },
    sourceDirectory in Pamflet := file("docs"),
    git.remoteRepo := "git@github.com:foundweekends/pamflet.git",
    name := "pamflet",
    publishArtifact := false
  ).
  aggregate(knockoff, library, app).
  enablePlugins(PamfletPlugin)
lazy val knockoff: Project =
  (project in file("knockoff")).
  settings(common: _*).
  settings(
    name := "pamflet-knockoff",
    description := "Extensions to the Knockoff Markdown parser",
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
    libraryDependencies ++= appDeps.value,
    resolvers += Resolver.typesafeIvyRepo("releases") // for launcher interface
  ).
  dependsOn(library)
