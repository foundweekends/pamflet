addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.3")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.3")

libraryDependencies += "org.foundweekends" %% "pamflet-library" % "0.8.0"
resolvers += Resolver.sonatypeRepo("public")
