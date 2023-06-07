import sbtrelease.ReleaseStateTransformations._

val unusedWarnings = Seq(
  "-Ywarn-unused"
)

val Scala212 = "2.12.17"

lazy val updateLaunchconfig = TaskKey[File]("updateLaunchconfig")

ThisBuild / evictionErrorLevel := Level.Warn

ThisBuild / scalaVersion       := Scala212
ThisBuild / organization       := "org.foundweekends"
ThisBuild / organizationName   := "foundweekends"
ThisBuild / crossScalaVersions := Seq(Scala212, "2.13.11", "3.3.0")
ThisBuild / homepage :=
  Some(url("https://www.foundweekends.org/pamflet/"))
ThisBuild  / licenses          := Seq("LGPL v3" -> url("https://www.gnu.org/licenses/lgpl.txt"))
ThisBuild / scmInfo            := Some(ScmInfo(url("https://github.com/foundweekends/pamflet"), "git@github.com:foundweekends/pamflet.git"))
ThisBuild / developers := List(
  Developer("n8han", "Nathan Hamblen", "@n8han", url("https://github.com/n8han")),
  Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
)
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val common = Seq(
  scalacOptions ++= Seq("-language:_", "-deprecation"),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 => Seq("-Yno-adapted-args", "-Xfuture")
      case _ => Nil
    }
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 11 => unusedWarnings
      case _ => Nil
    }
  },
  Test / publishArtifact := false,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    releaseStepTask(updateLaunchconfig),
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+ publishSigned"),
    releaseStepCommandAndRemaining("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
) ++ Seq(Compile, Test).flatMap(c =>
  c / console / scalacOptions --= unusedWarnings
)

lazy val knockoffDeps = Def.setting { Seq(
  "org.foundweekends" %% "knockoff" % "0.9.0"
)}
val unfilteredVersion = "0.12.0"
lazy val libraryDeps = Def.setting { Seq(
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "org.antlr" % "ST4" % "4.3.4"
)}
val launcherInterfaceVersion = "1.4.2"
lazy val appDeps = Def.setting { Seq(
  "org.scala-sbt" % "launcher-interface" % launcherInterfaceVersion % "provided"
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
          (Project extract s).runAggregated(extracted.get(thisProjectRef) / publishLocal, s)
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
    Pamflet / includeFilter := {
      new FileFilter{
        override def accept(file: File) = {
          !file.getCanonicalPath.contains("offline/")
        }
      }
    },
    updateLaunchconfig := {
      val mainClassName = (app / Compile / discoveredMainClasses).value match {
        case Seq(m) => m
        case zeroOrMulti => sys.error(s"could not found main class. $zeroOrMulti")
      }
      val launchconfig = s"""[app]
  version: ${(app / version).value}
  org: ${(app / organization).value}
  name: ${(app / normalizedName).value}
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
    Pamflet / sourceDirectory := (LocalRootProject / baseDirectory).value / "docs",
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

val jquery = "org.webjars" % "jquery" % "3.6.4"

lazy val library: Project = (project in file("library"))
  .dependsOn(knockoff)
  .settings(common)
  .settings(
    name := "pamflet-library",
    description := "Core Pamflet library",
    libraryDependencies ++= libraryDeps.value,
    libraryDependencies += jquery % Test, // for scala-steward
    Compile / resourceGenerators += Def.task {
      val Seq(jqueryJar) = dependencyResolution.value
        .retrieve(
          dependencyId = jquery,
          scalaModuleInfo = scalaModuleInfo.value,
          retrieveDirectory = csrCacheDirectory.value,
          log = streams.value.log
        )
        .left
        .map(e => throw e.resolveException)
        .merge
        .distinct
      val jqueryFileName = "jquery.min.js"

      IO.withTemporaryDirectory { tmpDir =>
        val Seq(jqueryFile) = IO.unzip(jqueryJar, tmpDir, _.split('/').last == jqueryFileName).toSeq
        IO.copy(
          Seq(
            jqueryFile -> (Compile / resourceManaged).value / "webroot" / "js" / jqueryFileName
          )
        ).toSeq
      }
    }.taskValue,
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.8.1" % Test,
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
  )
