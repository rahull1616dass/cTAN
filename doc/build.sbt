libraryDependencies ++= Seq(
	compilerPlugin("org.scala-lang.plugins" % ("scala-continuations-plugin_" + scalaVersion.value) % "1.0.2"),
	"com.github.ansell.pellet" % "pellet-owlapiv3" % "2.3.6-ansell" exclude("msv", "xsdlib")
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"
