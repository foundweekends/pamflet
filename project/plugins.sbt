addSbtPlugin("com.github.sbt" % "sbt-ghpages" % "0.7.0")
addSbtPlugin("com.github.sbt" % "sbt-site" % "1.6.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.8")

libraryDependencies += "org.foundweekends" %% "pamflet-library" % "0.12.0"
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always"
