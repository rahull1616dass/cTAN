/*
 * Copyright 2018 The SIRIS Project
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

/*
 * Copyright 2016 The SIRIS Project
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

import simx.components.ai.mipro.SemanticValueDSL
import simx.components.editor.EditorComponentAspect
import simx.core.entity.Entity
import simx.core.ontology.{EntityDescription, Symbols}
import simx.core.ontology.{types => semanticTypes}
import simx.core.svaractor.TimedRingBuffer.{At, Time}
import simx.core.svaractor.unifiedaccess.StateParticleInfo
import simx.core.svaractor.SVarActor
import simx.core.worldinterface.entity.filter.{HasSVal, SValEquals}
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}

import scala.collection.immutable



/**
  * @author Chris Zimmerer, Martin Fischbach
  *
  *
  */
object SemanticValueDSLExample extends SimXApplicationMain[SemanticValueDSLExample]{}

class SemanticValueDSLExample(args : Array[String]) extends SimXApplication with SemanticValueDSL {

  val Keyboard = semanticTypes.Semantics(Symbols.keyboard)

  /** ----------- Filter Definitions ----------- */
  // Depending on Semantics
  val Ball = SValEquals(semanticTypes.Semantics(Symbols.ball))
  val Box = SValEquals(semanticTypes.Semantics(Symbols.box))
  // Depending on Selection
  val SelectableEntities = HasSVal(semanticTypes.Selected)
  val SelectedEntities = SValEquals(semanticTypes.Selected(true))
  val UnselectedEntities = SValEquals(semanticTypes.Selected(false))

  /** Definition of required entities and properties for application*/
  Requires all properties from SelectableEntities
  //Requires property types.Selection from SelectableEntities
  //Requires properties (types.Selection and types.Activated) from SelectableEntities

  override protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    EditorComponentAspect('editor)

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {}

  protected def createEntities(): Unit = {
    // Creates an entity "Ball" with property Semantics and Selected
    new EntityDescription('Ball, semanticTypes.Semantics(Symbols.sphere)).realize{e =>
      e.set(semanticTypes.Selected(true))
    }
    // Creates an entity "Box" with property Semantics and Selected
    new EntityDescription('Box, semanticTypes.Semantics(Symbols.box)).realize{e =>
      e.set(semanticTypes.Selected(false))
    }
    // Creates an entity "Keyboard" with property Key_1 and Key_2
    new EntityDescription('Keyboard, Keyboard).realize{e =>
      e.set(semanticTypes.Key_1(false))
      e.set(semanticTypes.Key_2(false))
    }
  }

  def keyOnePressed(): Unit ={
    println("[info][SemanticValueDSLExample] --- Key_1 pressed ---")
    // Requests all selected entities
    val selectedEntities = all(SelectedEntities)
    println("[info][SemanticValueDSLExample] All selected entities: " + selectedEntities.map(_.getSimpleName))
    // Requests only one selected entity
    val oneSelectedEntity = one(SelectedEntities)
    println("[info][SemanticValueDSLExample] One Selected entities: " + oneSelectedEntity.getSimpleName)
  }

  def keyTwoPressed(): Unit ={
    println("[info][SemanticValueDSLExample] --- Key_2 pressed ---")
    // Requests the property Selected from entities described by "Ball"
    val selected = semanticTypes.Selected of Box
    println("[info][SemanticValueDSLExample] Selected of entitiy Box = " + selected.value)
    // Sets the property Selected to value true for all entities passing the "Box" filter
    Update the properties of Box `with` semanticTypes.Selected(!selected.value)
    println("[info][SemanticValueDSLExample] Set property Selected of entitiy Box = " + !selected.value)
  }

  protected def finishConfiguration(): Unit = {
    onOneEntityAppearance(SValEquals(Keyboard)){e =>
      e.observe(semanticTypes.Key_1){pressed => if(pressed){keyOnePressed()}}
      e.observe(semanticTypes.Key_2){pressed => if(pressed){keyTwoPressed()}}
    }
  }


  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {
    println("[info][SemanticValueDSLExample] Starting to observe property " + requirementInfo.identifier + " of entity " + e.getSimpleName)
  }

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: Time): Unit = {}

  protected def removeFromLocalRep(e : Entity){println("[info][ExampleApplication] Removed entity " + e)}
}





