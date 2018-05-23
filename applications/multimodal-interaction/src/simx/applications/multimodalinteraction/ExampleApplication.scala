/*
 * Copyright 2017 The SIRIS Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * The SIRIS Project is a cooperation between Beuth University, Berlin and the
 * HCI Group at the University of Würzburg. The project is funded by the German
 * Federal Ministry of Education and Research (grant no. 17N4409).
 */

package simx.applications.multimodalinteraction

import simx.components.editor.EditorComponentAspect
import simx.components.synchronization.unity.UnityComponentAspect
import simx.core.entity.Entity
import simx.core.ontology.Symbols
import simx.core.ontology.types.OntologySymbol
import simx.core.svaractor.SVarActor
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}

import collection.immutable

/**
  * @author Chris Zimmerer, Martin Fischbach
  */
object ExampleApplication extends SimXApplicationMain[ExampleApplication] {}

class ExampleApplication(args : Array[String]) extends SimXApplication
{
  //Component names
  val editorName = 'editor
  val unityName = 'unity

  override protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    UnityComponentAspect(unityName, "localhost", 8000) and
    EditorComponentAspect(editorName, appName = "MasterControlProgram")

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {}

  protected def createEntities(): Unit = {}

  protected def finishConfiguration(): Unit = {
    println("[info][ExampleApplication] Hello World!")
  }

  override protected def removeFromLocalRep(e: Entity): Unit = {}
}





