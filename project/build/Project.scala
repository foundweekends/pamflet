import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  val uf_version = "0.3.2"
  
  // unfiltered
  lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
  lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
}
