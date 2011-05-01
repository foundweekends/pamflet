import sbt._

class Project(info: ProjectInfo) extends ParentProject(info)
    with conscript.Harness
    with pamflet.Actions {
  lazy val library = project("library", "pamflet", new DefaultProject(_) {
    val uf_version = "0.3.2"

    // unfiltered
    val uff = "net.databinder" %% "unfiltered-filter" % uf_version
    val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
    val knockoff  = "com.tristanhunt" %% "knockoff" % "0.8.0-16"
    val stringTemplate = "org.antlr" % "stringtemplate" % "3.2.1"
  })
  lazy val plugin = project("plugin", "pamflet plugin", 
                            new PluginProject(_), library)
  lazy val app = project("app", 
                         "pamflet app", 
                         new DefaultProject(_) {
    val launch = "org.scala-tools.sbt" % "launcher-interface" % "0.7.4" % "provided"
  }, library)
}
