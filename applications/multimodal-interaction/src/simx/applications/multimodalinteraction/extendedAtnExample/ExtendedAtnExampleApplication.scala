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

package simx.applications.multimodalinteraction.extendedAtnExample

import simplex3d.math.floatx.ConstVec3f
import simx.applications.multimodalinteraction.extendedAtnExample.atn.{ExtendedExampleAtnParser, ExtendedExampleWords}
import simx.applications.multimodalinteraction.io.SpeechSimulator
import simx.components.ai.atn.interaction.lexicon.Lexicon
import simx.components.editor.EditorComponentAspect
import simx.core.entity.Entity
import simx.core.ontology.{EntityDescription, Symbols, types}
import simx.core.svaractor.{SVarActor, TimedRingBuffer}
import simx.core.svaractor.SVarActor.Ref
import simx.core.worldinterface.eventhandling.EventHandler
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}
import simx.components.ai.atn.{Events => AtnEvents}
import simx.components.ai.atn.ontology.{types => lexiconTypes}
import simx.components.ai.mipro.SemanticValueDSL
import simx.components.synchronization.unity.{UnityComponent, UnityComponentAspect}
import simx.core.svaractor.unifiedaccess.StateParticleInfo
import simx.core.worldinterface.entity.filter.SValEquals



object ExtendedAtnExampleApplication extends SimXApplicationMain (new ExtendedAtnExampleApplication) {}


class ExtendedAtnExampleApplication extends SimXApplication with EventHandler with SemanticValueDSL
{

  val useUnity = true

  val PrefabFactory = SValEquals(types.Semantics(Symbols.entityCreation))

  Requires all properties from PrefabFactory


  protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    EditorComponentAspect('editor) and
    UnityComponentAspect('unity, "localhost", 8000) iff useUnity



  protected def configureComponents(components: Map[Symbol, Ref]): Unit = {
    SVarActor.createActor(new ExtendedExampleAtnParser('myATN, _autoResetAfter = Some(5000L)))
    SVarActor.createActor(SpeechSimulator())

    Lexicon.clear()
    Lexicon.put("select", ExtendedExampleWords.Selection())
    Lexicon.put("move", ExtendedExampleWords.Translation())
    Lexicon.put("create", ExtendedExampleWords.Creation())
    Lexicon.put("make", ExtendedExampleWords.Scale())
    Lexicon.put("a", ExtendedExampleWords.Article())
    Lexicon.put("ball", ExtendedExampleWords.Ball())
    Lexicon.put("box", ExtendedExampleWords.Box())
    Lexicon.put("bigger", ExtendedExampleWords.Bigger())
    Lexicon.put("big", ExtendedExampleWords.Big())
    Lexicon.put("there", ExtendedExampleWords.Existential())
  }

  protected def createEntities(): Unit = {
    if(!useUnity) createTestEntities()
  }

  protected def finishConfiguration(): Unit = {
    AtnEvents.command.observe{ newCommand =>
      val values = newCommand.values
      val action = values.firstValueFor(lexiconTypes.Verb).actions.head
      val entity = values.firstValueFor(types.Entity)
      if(action == Symbols.selection) {
        selectEntity(entity)
      }
      if(action == Symbols.move) {
        val raycastHit = values.getFirstValueFor(types.RaycastHit)
        if(raycastHit.isDefined){
          moveEntity(entity, raycastHit.get)
        }
      }
      if(action == Symbols.entityCreation){
        val noun = values.getFirstValueFor(lexiconTypes.Noun)
        if(noun.isDefined) {
          entityCreation(noun.get.entityRelation.toSymbol.name)
        }
      }
      if(action == Symbols.scale) {
        val adj = values.getFirstValueFor(lexiconTypes.Adjective)
        if(adj.isDefined){
          val property = adj.get.property
          val semanticValueScale = property.generate().asInstanceOf[types.Scale]
          scaleEntity(entity, semanticValueScale.value)
        }
      }
    }
  }

  private def entityCreation(name: String): Unit = {
    Update the properties of PrefabFactory `with` UnityComponent.createPrefabReference(name)
  }

  private def scaleEntity(e: Entity, scale: ConstVec3f): Unit = {
    e.get(types.Scale){currentScale =>
      val newScale = currentScale * scale.x
      e.set(types.Scale(newScale))
    }
  }

  private def moveEntity(e: Entity, raycastHit: ConstVec3f): Unit ={
    e.set(types.TargetPosition(raycastHit))
  }

  private def selectEntity(e: Entity): Unit ={
    e.set(types.Selected(true))
  }

  protected def removeFromLocalRep(e: Entity) {}

  private def createTestEntities(): Unit ={
    new EntityDescription('Ball, types.Semantics(Symbols.ball)).realize{e =>
      e.set(types.Selected(false))
    }
    new EntityDescription('Ball2, types.Semantics(Symbols.ball)).realize{e =>
      e.set(types.Selected(false))
    }
    new EntityDescription('Box, types.Semantics(Symbols.box)).realize{e =>
      e.set(types.Selected(false))
    }
  }


  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: TimedRingBuffer.Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {}
}