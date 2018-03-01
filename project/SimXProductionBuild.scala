import sbt._


object SimXProductionBuild extends SimXBuildBase{
  // version settings
  val usedJavaVersion = "1.8"
  val usedScalaVersion = "2.11.8"
  val projectName = "simx-production"

  override def rootProject =
    Some(basicexamples)


  /* Components */
  //Ai
  lazy val atn		        = SimXComponent ( id = "atn", 	     	       base = file( "components/ai/atn")).
    dependsOn( core, planning )
  lazy val mipro	      = SimXComponent ( id = "mipro", 	         base = file( "components/ai/mipro")).
    dependsOn( core )
  lazy val nlp	          = SimXComponent ( id = "nlp", 	             base = file( "components/ai/nlp")).
    dependsOn( core )
  lazy val planning       = SimXComponent ( id = "planning", 	     	   base = file( "components/ai/planning")).
    dependsOn( core % "test->test;compile->compile" )
  lazy val reasoning      = SimXComponent ( id = "reasoning", 	       base = file( "components/ai/reasoning")).
    dependsOn( core )
  lazy val unification	  = SimXComponent ( id = "unification", 	     	       base = file( "components/ai/unification")).
    dependsOn( core )

  //Editor
  lazy val editor         = SimXComponent ( id = "editor", 		         base = file( "components/editor")).
    dependsOn( core )

  lazy val webeditor         = SimXComponent ( id = "webeditor",             base = file( "components/webeditor")).
    dependsOn( core )
    
  lazy val webui         = SimXComponent ( id = "webui",             base = file( "components/webui")).
    dependsOn( core )    
  //IO
  lazy val cv	            = SimXComponent ( id = "cv", 	               base = file( "components/io/cv")).
    dependsOn( core )

  lazy val tuio           = SimXComponent ( id = "tuio", 	             base = file( "components/io/tuio")).
    dependsOn( core )

//  lazy val sphinx         = SimXComponent ( id = "sphinx", 	           base = file( "components/io/sphinx")).
//    dependsOn( core )

  lazy val json           = SimXComponent ( id = "json", 	             base = file( "components/io/json")).
    dependsOn( core )

  lazy val vrpn           = SimXComponent ( id = "vrpn", 		           base = file( "components/io/vrpn")).
    dependsOn( core )

  lazy val j4k            = SimXComponent ( id = "j4k", 	 	           base = file( "components/io/j4k")).
    dependsOn( rendering )

  lazy val leapmotion     = SimXComponent ( id = "leapmotion",         base = file( "components/io/leapmotion")).
    dependsOn( core )

  //Physics
  lazy val physics       = SimXComponent ( id = "physics", 	      base = file( "components/physics/physics")).
    dependsOn( core )
	
  lazy val jbullet 	      = SimXComponent ( id = "jbullet", 		       base = file( "components/physics/jbullet" )).
    dependsOn( physics )
  //Remote
  lazy val remote         = SimXComponent ( id = "remote", 	 	         base = file( "components/remote")).
    dependsOn( core )

  //Rendering
  lazy val gui            = SimXComponent ( id = "gui", 		           base = file( "components/rendering/gui")) .
    dependsOn( jvr, tuio, editor )

  lazy val rendering       = SimXComponent ( id = "rendering", 	      base = file( "components/rendering/rendering")).
    dependsOn( core )

  lazy val jvr            = SimXComponent ( id = "jvr", 		           base = file( "components/rendering/jvr")).
    dependsOn( rendering )

  lazy val unity            = SimXComponent ( id = "unity", 		           base = file( "components/synchronization/unity")).
    dependsOn( json, rendering, physics, synchronization )


  //Sound
  lazy val lwjgl_sound    = SimXComponent ( id = "lwjgl_sound",        base = file( "components/sound/lwjgl-sound")).
    dependsOn( core )

  lazy val java_sound     = SimXComponent ( id = "java-sound",         base = file( "components/sound/java-sound")).
    dependsOn( core )
	
  //Synchronization
  lazy val synchronization  = SimXComponent ( id = "synchronization", 	      base = file( "components/synchronization/synchronization")).
    dependsOn( core )	

  /* Applications*/
  lazy val aiexamples		  = SimXApplication ( id = "examples-ai",        base = file( "applications/examples/ai")).
    dependsOn(core, jvr, jbullet, tuio, editor, vrpn, mipro, atn, remote, planning, reasoning, nlp, j4k, leapmotion, gui, lwjgl_sound).
    aggregate(core, jvr, jbullet, tuio, editor, vrpn, mipro, atn, remote, planning, reasoning, nlp, j4k, leapmotion, gui, lwjgl_sound)

  lazy val mmiexamples		  = SimXApplication ( id = "examples-mmi",        base = file( "applications/examples/mmi")).
    dependsOn(core, jvr, jbullet, tuio, editor, vrpn, mipro, atn, remote, planning, reasoning, nlp, j4k, leapmotion, gui, lwjgl_sound, unification, json).
    aggregate(core, jvr, jbullet, tuio, editor, vrpn, mipro, atn, remote, planning, reasoning, nlp, j4k, leapmotion, gui, lwjgl_sound, unification, json)

  lazy val raycalib		  = SimXApplication ( id = "raycalib",        base = file( "applications/raycalib")).
    dependsOn(core, jvr, vrpn, gui).
    aggregate(core, jvr, vrpn, gui)

  lazy val basicexamples  = SimXApplication ( id = "examples-basic",     base = file( "applications/examples/basic")).
    dependsOn(core, jbullet, jvr, tuio, lwjgl_sound, editor, vrpn, remote, cv, gui, j4k, webeditor).
    aggregate(core, jbullet, jvr, tuio, lwjgl_sound, editor, vrpn, remote, cv, gui, j4k, webeditor)
    
  lazy val extendedexamples  = SimXApplication ( id = "examples-extended",     base = file( "applications/examples/extended")).
    dependsOn(core, jbullet, jvr, lwjgl_sound, editor, webui, vrpn, webeditor).
    aggregate(core, jbullet, jvr, lwjgl_sound, editor, webui, vrpn, webeditor)    

  lazy val benchmarks  = SimXApplication ( id = "benchmarks",     base = file( "applications/benchmarks")).
    dependsOn(core, jbullet, jvr, lwjgl_sound, editor).
    aggregate(core, jbullet, jvr, lwjgl_sound, editor)

  lazy val unityexamples  = SimXApplication ( id = "examples-unity",     base = file( "applications/examples/unity")).
    dependsOn(core, jbullet, jvr, unity, lwjgl_sound, editor ).
    aggregate(core, jbullet, jvr, unity, lwjgl_sound, editor )

}

