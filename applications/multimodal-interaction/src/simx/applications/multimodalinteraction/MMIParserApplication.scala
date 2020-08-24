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

import simplex3d.math.floatx.ConstVec3f
import simx.applications.multimodalinteraction.mmiparser.{ExampleWords, MMIParser}
import simx.applications.multimodalinteraction.io.{Servers, SpeechRecognizer, SpeechSimulator}
import simx.components.ai.atn.interaction.lexicon.Lexicon
import simx.components.ai.atn.ontology.{types => lexiconTypes}
import simx.components.ai.atn.{Events => AtnEvents}
import simx.components.ai.mipro.SemanticValueDSL
import simx.components.editor.EditorComponentAspect
import simx.components.synchronization.unity.{UnityComponent, UnityComponentAspect}
import simx.components.vrpn.VRPNComponentAspect
import simx.core.entity.Entity
import simx.core.entity.typeconversion.TypeInfo
import simx.core.ontology.{EntityDescription, GroundedSymbol, Symbols, types}
import simx.core.svaractor.{SVarActor, TimedRingBuffer}
import simx.core.svaractor.SVarActor.Ref
import simx.core.svaractor.semantictrait.base.Base
import simx.core.svaractor.unifiedaccess.StateParticleInfo
import simx.core.worldinterface.entity.filter.SValEquals
import simx.core.worldinterface.eventhandling.EventHandler
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}



object MMIParserApplication extends SimXApplicationMain (new MMIParserApplication) {}


class MMIParserApplication extends SimXApplication with EventHandler with SemanticValueDSL
{
  val PrefabFactory = SValEquals(types.Semantics(Symbols.entityCreation))
  Requires all properties from PrefabFactory

  val keyboardMode = true

  protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    EditorComponentAspect('editor) and
    UnityComponentAspect('unity, "localhost", 8000) and
    VRPNComponentAspect('vrpn) iff !keyboardMode


  protected def configureComponents(components: Map[Symbol, Ref]): Unit = {
    var autoreset = Some(5000L);
    if(keyboardMode) SVarActor.createActor(SpeechSimulator())
    else {
      autoreset = Some(1000L);
      SVarActor.createActor(SpeechRecognizer(Servers.localhost))
    }

    SVarActor.createActor(new MMIParser('myATN, _autoResetAfter = autoreset))

    Lexicon.clear()
    Lexicon.put("create", ExampleWords.Creation())
    Lexicon.put("select", ExampleWords.Selection())
    Lexicon.put("move", ExampleWords.Translation())
    Lexicon.put("a", ExampleWords.Article())
    Lexicon.put("the", ExampleWords.Article())
    Lexicon.put("that", ExampleWords.Article())
    Lexicon.put("ball", ExampleWords.Ball())
    Lexicon.put("there", ExampleWords.Existential())
    Lexicon.put("myself", ExampleWords.Myself())
  }

  protected def createEntities(): Unit = {
  }

  protected def finishConfiguration(): Unit = {
    // defines the reaction to an AtnEvents.command from the MMIParser by checking which action was recognized by the cATN
    AtnEvents.command.observe{ newCommand =>
      val values = newCommand.values
      val action = values.firstValueFor(lexiconTypes.Verb).actions.head
      if(action == Symbols.move) {
        val entity = values.getFirstValueFor(types.Entity)
        val raycastHit = values.getFirstValueFor(types.RaycastHit)
        if(raycastHit.isDefined && entity.isDefined){
          moveEntity(entity.get, raycastHit.get)
        } else {
          println("No entity or raycastHit found in ATN command!")
        }
      }

      if(action == Symbols.entityCreation){
        val noun = values.getFirstValueFor(lexiconTypes.Noun)
        if(noun.isDefined) {
          entityCreation(noun.get.entityRelation.toSymbol.name)
        } else {
          println("No entity found in ATN command!")
        }
      }

      if(action == Symbols.entityDeletion) {
        val entity = values.getFirstValueFor(types.Entity)
        if(entity.isDefined){deleteEntity(entity.get)}else{println("No entity found in ATN command!")}
      }

      //TODO: Missing Select Action
    }
  }

  private def entityCreation(name: String): Unit = {
    Update the properties of PrefabFactory `with` UnityComponent.createPrefabReference(name)
  }

  private def deleteEntity(e: Entity): Unit ={
    e.remove()
  }

  private def moveEntity(e: Entity, raycastHit: ConstVec3f): Unit ={
    e.set(types.TargetPosition(raycastHit))
  }

  private def selectEntity(e: Entity): Unit ={
    e.set(types.Selected(true))
  }

  protected def removeFromLocalRep(e: Entity) {}

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: TimedRingBuffer.Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {}
}