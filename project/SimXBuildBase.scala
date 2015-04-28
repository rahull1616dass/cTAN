import sbt._
import sbt.Keys._
import sbtunidoc.Plugin.UnidocKeys._

trait SimXBuildBase extends Build with SimXSettings{
  object SimXComponent {
    def apply(id: String, base: File) = Project(id, base, settings = buildSettings ++ docSettings)
  }

  object SimXApplication {
    def apply(id: String, base: File) =
      Project(id, base, settings = buildSettings ++ docSettings ++ Seq(
        setOntoDir := updateWorkingDirectory(baseDirectory.value),
        compile in Compile <<= compile in Compile dependsOn setOntoDir
      ))
  }

  lazy val ontologyGen = Project(
    id = "ontology-generator",
    base = file( "components/ontology/generating"),
    settings = basicBuildSettings ++ Seq(
      generateOntology := inWorkingDirectory{ dir =>
        val wdPath = dir.getAbsolutePath
        resetWorkingDirectory()
        toError((runner in Compile).value.run(
          mainClass = mainClass.value.getOrElse(throw new Exception("No 'mainClass' defined in build.sbt.")),
          classpath = (fullClasspath in Compile).value.map(_.data),
          options = Seq(simxBase.getAbsolutePath, wdPath), //program arguments
          log = streams.value.log)
        )
      }
    )
  )

  /* SimX Core */
  lazy val core = Project( id = "core", base = file( "core"),
    settings = buildSettings ++ docSettings ++ Seq(
      compile in Compile <<= compile in Compile dependsOn callOntoGen
    )
  )

  /* Documentation */
  lazy val documentation = Project( id = "doc", base = file( "doc"),
    settings = buildSettings ++ Seq(
      aggregate := false,
      setOntoDir := { updateWorkingDirectory(baseDirectory.value, force = true) },
      compile in Compile <<= compile in Compile dependsOn setOntoDir,
      unidoc in Compile <<= unidoc in Compile dependsOn (setOntoDir, callOntoGen),
      doc in Compile := (unidoc in Compile).value.headOption getOrElse file(".")
    )
  )
}

