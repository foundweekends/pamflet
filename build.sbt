import sbtrelease.ReleaseStateTransformations._

val unusedWarnings = Seq(
  "-Ywarn-unused"
)

val Scala212 = "2.12.10"

lazy val updateLaunchconfig = TaskKey[File]("updateLaunchconfig")

ThisBuild / scalaVersion       := Scala212
ThisBuild / organization       := "org.foundweekends"
ThisBuild / organizationName   := "foundweekends"
ThisBuild / crossScalaVersions := Seq(Scala212)
ThisBuild / homepage :=
  Some(new java.net.URL("http://www.foundweekends.org/pamflet/"))
ThisBuild  / licenses          := Seq("LGPL v3" -> url("http://www.gnu.org/licenses/lgpl.txt"))
ThisBuild / scmInfo            := Some(ScmInfo(url("https://github.com/foundweekends/pamflet"), "git@github.com:foundweekends/pamflet.git"))
ThisBuild / developers := List(
  Developer("n8han", "Nathan Hamblen", "@n8han", url("https://github.com/n8han")),
  Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
)
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")

lazy val common = Seq(
  scalacOptions ++= Seq("-language:_", "-deprecation", "-Xfuture"),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 => Seq("-Yno-adapted-args")
      case _ => Nil
    }
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 11 => unusedWarnings
      case _ => Nil
    }
  },
  publishArtifact in Test := false,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    releaseStepTask(updateLaunchconfig),
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
) ++ Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

lazy val knockoffDeps = Def.setting { Seq(
  "org.foundweekends" %% "knockoff" % "0.8.6"
)}
val unfilteredVersion = "0.9.1"
val stringtemplateVersion = "3.2.1"
lazy val libraryDeps = Def.setting { Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.antlr" % "stringtemplate" % stringtemplateVersion
)}
val launcherInterfaceVersion = "1.1.2"
val servletApiVersion = "3.1.0"
lazy val appDeps = Def.setting { Seq(
  "org.scala-sbt" % "launcher-interface" % launcherInterfaceVersion % "provided",
  "javax.servlet" % "javax.servlet-api" % servletApiVersion
)}

val launchconfigFile = file("src/main/conscript/pf/launchconfig")

lazy val pamflet: Project = (project in file("."))
  .enablePlugins(GhpagesPlugin, ConscriptPlugin, PamfletPlugin)
  .aggregate(knockoff, library, app)
  .settings(common)
  .settings(
    {
      val out = file("target/test.html")
      TaskKey[File]("testConscript") := Def.sequential(
        updateLaunchconfig,
        Def.task {
          val extracted = Project extract state.value
          val s = extracted.appendWithSession(Seq(scalaVersion := Scala212), state.value)
          (Project extract s).runAggregated(publishLocal in extracted.get(thisProjectRef), s)
          IO.delete(out)
        },
        csRun.toTask(" pf src/test/pf target"),
        Def.task {
          sys.process.Process(s"git checkout ${launchconfigFile}").!
          assert(out.exists)
          out
        }
      ).value
    },
    includeFilter in Pamflet := {
      new FileFilter{
        override def accept(file: File) = {
          !file.getCanonicalPath.contains("offline/")
        }
      }
    },
    updateLaunchconfig := {
      val mainClassName = (discoveredMainClasses in Compile in app).value match {
        case Seq(m) => m
        case zeroOrMulti => sys.error(s"could not found main class. $zeroOrMulti")
      }
      val launchconfig = s"""[app]
  version: ${(version in app).value}
  org: ${(organization in app).value}
  name: ${(normalizedName in app).value}
  class: ${mainClassName}
[scala]
  version: ${Scala212}
[repositories]
  local
  maven-central
"""
      IO.write(launchconfigFile, launchconfig)
      launchconfigFile
    },
    sourceDirectory in Pamflet := file("docs"),
    git.remoteRepo := "git@github.com:foundweekends/pamflet.git",
    name := "pamflet",
    publishArtifact := false,
    previewSite / aggregate := false,
  )

lazy val knockoff: Project = (project in file("knockoff"))
  .settings(common)
  .settings(
    name := "pamflet-knockoff",
    description := "Extensions to the Knockoff Markdown parser",
    libraryDependencies ++= knockoffDeps.value
  )

lazy val library: Project = (project in file("library"))
  .dependsOn(knockoff)
  .settings(common)
  .settings(
    name := "pamflet-library",
    description := "Core Pamflet library",
    libraryDependencies ++= libraryDeps.value,
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.4" % Test,
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )

lazy val app: Project = (project in file("app"))
  .dependsOn(library)
  .settings(common)
  .settings(
    name := "pamflet-app",
    description :=
      "Pamflet app for previewing and publishing project documentation",
    libraryDependencies ++= appDeps.value,
    resolvers += Resolver.typesafeIvyRepo("releases") // for launcher interface
  )
