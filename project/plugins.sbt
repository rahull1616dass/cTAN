addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("org.netbeans.nbsbt" % "nbsbt-plugin" % "1.1.2")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.3")

resolvers += Classpaths.sbtPluginReleases

resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
