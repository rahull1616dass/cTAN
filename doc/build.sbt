libraryDependencies ++= Seq(
	compilerPlugin("org.scala-lang.plugins" % ("scala-continuations-plugin_" + scalaVersion.value) % "1.0.2"),
	"net.sourceforge.owlapi" % "owlapi-distribution" % "3.5.0"
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"
