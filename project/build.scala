import sbt._

object PamfletBuild extends Build {
  lazy val pamflet =
    Project("pamflet", file(".")) aggregate(app, plugin)
  lazy val knockoff: Project =
    Project("pamflet-knockoff", file("knockoff"),
            delegates = pamflet :: Nil)
  lazy val library: Project =
    Project("pamflet-library", file("library"),
            delegates = pamflet :: Nil) dependsOn knockoff
  lazy val app: Project =
    Project("pamflet-app", file("app"),
            delegates = pamflet :: Nil) dependsOn library
  lazy val plugin: Project =
    Project("pamflet-plugin", file("plugin"),
            delegates = pamflet :: Nil) dependsOn library
}
