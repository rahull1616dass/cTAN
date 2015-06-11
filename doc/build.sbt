libraryDependencies ++= Seq(
	compilerPlugin("org.scala-lang.plugins" % ("scala-continuations-plugin_" + scalaVersion.value) % "1.0.2")
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"

ivyXML := 
	<dependencies>
		<dependency org="com.github.ansell.pellet" name="pellet-owlapiv3" rev="2.3.6-ansell">
			<!-- xsdlib brings very old xerces implementation, exclude it-->
			<exclude org="msv" module="xsdlib" />
		</dependency>
	</dependencies>
