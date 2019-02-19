logLevel := Level.Warn

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2")

addSbtPlugin("org.xerial.sbt" %% "sbt-sonatype" % "2.4")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.2.7")