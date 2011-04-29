import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {
  lazy val library = project("library", "pamflet", new DefaultProject(_) {
    val uf_version = "0.3.2"

    // unfiltered
    lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
    lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
    lazy val knockoff  = "com.tristanhunt" %% "knockoff" % "0.8.0-16"
  })
}
