import sbtunidoc.Plugin._
import UnidocKeys._
import sbt._
import Keys._

trait SimXSettings extends Build{
  val usedScalaVersion : String
  val usedJavaVersion : String
  val projectName : String

  val documentation : Project
  val ontologyGen : Project

  // global settings
  override lazy val settings = super.settings ++ Seq(
    javacOptions in Compile ++= Seq("-source", usedJavaVersion, "-target", usedJavaVersion, "-Xlint:unchecked", "-Xlint:deprecation"),
    name := projectName
  )

  val basicBuildSettings = Defaults.coreDefaultSettings ++ Defaults.defaultConfigs ++ Seq(
    javacOptions in Compile ++= Seq("-source", usedJavaVersion, "-target", usedJavaVersion, "-Xlint:unchecked", "-Xlint:deprecation"),
    organization := "The Siris Team",
    scalaVersion := usedScalaVersion,
    version := "1.0"
  )

  //  task keys for internal usage
  lazy val generateOntology = taskKey[Unit]("Generates the ontology for the current SimX application project.")
  lazy val callOntoGen = taskKey[Unit]("Calls the ontology generation task.")
  lazy val setOntoDir =
    taskKey[Unit]("Sets the working directory for the ontology generation task to the projects baseDirectory.")

  protected val simxBase = file(".")
  //TODO: Check for thread safety in unusual cases(e.g. when compiling multiple SimXApplications at once)
  private var workingDirectory: Option[File] = None
  private var lastWDUpdate = -1L

  protected def updateWorkingDirectory(f : File, force : Boolean = false ): Unit ={
    val now = System.currentTimeMillis()
    if (force || now - lastWDUpdate > 500 || workingDirectory.isEmpty){
      workingDirectory = Some(f)
      lastWDUpdate = now
    }
  }

  protected def resetWorkingDirectory(){
    workingDirectory = None
  }

  protected def inWorkingDirectory( handler : File => Unit): Unit ={
    workingDirectory.collect{ case f => handler(f) }
  }

  protected lazy val buildSettings = basicBuildSettings ++ unidocSettings ++ Seq(
    callOntoGen := (generateOntology in ontologyGen).value,
    target in unidoc in ScalaUnidoc := crossTarget.value / "api",
    doc in Compile := (doc in ScalaUnidoc).value
  )

  protected lazy val docSettings = Seq(
    unidoc in Compile <<= unidoc in documentation in Compile,
    doc in Compile := { doc in documentation in Compile }.value
  )
}