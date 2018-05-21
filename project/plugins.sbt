addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.2")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.2")

libraryDependencies += "org.foundweekends" %% "pamflet-library" % "0.7.2"
resolvers += Resolver.sonatypeRepo("public")
