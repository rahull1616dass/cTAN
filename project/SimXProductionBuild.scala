import sbt._


object SimXProductionBuild extends SimXBuildBase {
  // version settings
  val usedJavaVersion = "1.8"
  val usedScalaVersion = "2.11.8"
  val projectName = "simx-production"

  override def rootProject =
    Some(multimodalinteraction)


  /* Components */
  lazy val atn		        = SimXComponent ( id = "atn", 	     	       base = file( "components/ai/atn")).
    dependsOn( core, planning )

  lazy val mipro	      = SimXComponent ( id = "mipro", 	         base = file( "components/ai/mipro")).
    dependsOn( core )

  lazy val nlp	          = SimXComponent ( id = "nlp", 	             base = file( "components/ai/nlp")).
    dependsOn( core )

  lazy val unification	  = SimXComponent ( id = "unification", 	     	       base = file( "components/ai/unification")).
    dependsOn( core )

  lazy val planning       = SimXComponent ( id = "planning", 	     	   base = file( "components/ai/planning")).
    dependsOn( core % "test->test;compile->compile" )

  lazy val editor         = SimXComponent ( id = "editor", 		         base = file( "components/editor")).
    dependsOn( core )

  lazy val json           = SimXComponent ( id = "json", 	             base = file( "components/io/json")).
    dependsOn( core )

  lazy val vrpn           = SimXComponent ( id = "vrpn", 		           base = file( "components/io/vrpn")).
    dependsOn( core )

  lazy val unity            = SimXComponent ( id = "unity", 		           base = file( "components/synchronization/unity")).
    dependsOn( json, synchronization )

  lazy val synchronization  = SimXComponent ( id = "synchronization", 	      base = file( "components/synchronization/synchronization")).
    dependsOn( core )

	
  lazy val multimodalinteraction  = SimXApplication ( id = "multimodal-interaction",     base = file( "applications/multimodal-interaction")).
    dependsOn(core, unity, editor, mipro, atn, vrpn).
    aggregate(core, unity, editor, mipro, atn, vrpn)

}

