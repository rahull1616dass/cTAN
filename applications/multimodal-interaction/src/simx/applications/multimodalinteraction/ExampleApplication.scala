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

import simplex3d.math.floatx.ConstVec3f
import simx.components.editor.EditorComponentAspect
import simx.components.synchronization.unity.UnityComponentAspect
import simx.core.entity.Entity
import simx.core.ontology.Symbols
import simx.core.ontology.types.OntologySymbol
import simx.core.svaractor.SVarActor
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}
import simx.core.ontology.{types => semanticTypes}
import simx.core.worldinterface.entity.filter.{HasSVal, SValEquals}
import simx.core.svaractor.unifiedaccess.StateParticleInfo
import simx.core.svaractor.TimedRingBuffer.{At, Time}
import simx.components.ai.mipro.SemanticValueDSL
import simx.core.ontology.{EntityDescription, Symbols}

import collection.immutable

/**
  * @author Chris Zimmerer, Martin Fischbach
  */
object ExampleApplication extends SimXApplicationMain[ExampleApplication] {}

class ExampleApplication(args : Array[String]) extends SimXApplication with SemanticValueDSL
{
  //Component names
  val editorName = 'editor
  val unityName = 'unity

  val Ball = SValEquals(semanticTypes.Semantics(Symbols.ball))
  val InputManager = semanticTypes.Semantics(Symbols.keyboard)
  val SelectableEntities = HasSVal(semanticTypes.Selected)
  val SelectedEntities   = SValEquals(semanticTypes.Selected(true))

  /** Definition of required entities and properties for application*/
  Requires all properties from SelectableEntities
  override protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    UnityComponentAspect(unityName, "localhost", 8000) and
    EditorComponentAspect(editorName, appName = "MasterControlProgram")

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {}

  protected def createEntities(): Unit = {
    new EntityDescription('InputManager, InputManager).realize{e =>
      e.set(semanticTypes.Key_1(false))
      e.set(semanticTypes.Key_2(false))
    }
  }

  def keyOnePressed(): Unit ={
    println("[info][ExampleApplication] --- Key_1 pressed ---")
    // Requests all selected entities
    val selectedEntities = Get all SelectedEntities
    println("[info][ExampleApplication] All selected entities: " + selectedEntities.map(_.getSimpleName))
  }

  def keyTwoPressed(): Unit ={
    println("[info][ExampleApplication] --- Key_2 pressed ---")
    // Requests all Balls
    val balls = Get all Ball
    // Sets the targerposition to a random value between 0 and 1 for every ball
    balls.foreach{ ball =>
      val random = scala.util.Random
      val newValue = ConstVec3f(random.nextFloat(), random.nextFloat(), random.nextFloat())
      ball.set(semanticTypes.TargetPosition(newValue))
      println("[info][ExampleApplication] Set property TargetPosition of entity with Name " + ball.getSimpleName + " to value " + newValue)
    }

  }

  protected def finishConfiguration(): Unit = {
    onOneEntityAppearance(SValEquals(InputManager)){e =>
      e.observe(semanticTypes.Key_1){pressed => if(pressed){keyOnePressed()}}
      e.observe(semanticTypes.Key_2){pressed => if(pressed){keyTwoPressed()}}
    }
  }

  override protected def removeFromLocalRep(e: Entity): Unit = {}

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {
    println("[info][ExampleApplication] Starting to observe property " + requirementInfo.identifier + " of entity " + e.getSimpleName)
  }

}





