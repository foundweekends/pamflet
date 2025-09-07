addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.9")

libraryDependencies += "org.foundweekends" %% "pamflet-library" % "0.13.0"
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always"
