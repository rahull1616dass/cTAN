libraryDependencies ++= Seq(
	compilerPlugin("org.scala-lang.plugins" % ("scala-continuations-plugin_" + scalaVersion.value) % "1.0.2") // "2.11.8"
)

scalaSource in Compile := baseDirectory.value / "src"

unmanagedJars in Compile := ((baseDirectory.value ** "lib") ** "*.jar").classpath

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"

classDirectory in Compile := target.value / "scala/classes"

classDirectory in Test := target.value / "scala/test-classes"

fork := false

unmanagedClasspath in Runtime +=  baseDirectory.value / "configs" // (baseDirectory) map { bd => Attributed.blank(bd / "configs") }

baseDirectory in run := baseDirectory.value

javaOptions in run += "-Xmx2G" 

javaOptions in run += "-Xms1G"

assemblyJarName in assembly := "something.jar"

mainClass in assembly := Some("simx.applications.multimodalinteraction.AtnExampleApplication")