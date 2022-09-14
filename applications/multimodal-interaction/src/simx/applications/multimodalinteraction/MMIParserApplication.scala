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
import akka.util.Helpers.Requiring
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
import simx.core.ontology.types.OntologySymbol
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
    Lexicon.put("select", ExampleWords.Selection())
    Lexicon.put("deselect", ExampleWords.Deselection())
    Lexicon.put("move", ExampleWords.Translation())
    Lexicon.put("delete", ExampleWords.Deletion())
    Lexicon.put("destroy", ExampleWords.Deletion())
    Lexicon.put("create", ExampleWords.Creation())
    Lexicon.put("a", ExampleWords.Article())
    Lexicon.put("the", ExampleWords.Article())
    Lexicon.put("that", ExampleWords.Article())
    Lexicon.put("ball", ExampleWords.Ball())
    Lexicon.put("there", ExampleWords.Existential())
    Lexicon.put("color", ExampleWords.Coloration())
    Lexicon.put("green", ExampleWords.Green())
    Lexicon.put("blue", ExampleWords.Blue())
    Lexicon.put("red", ExampleWords.Red())
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
        moveEntity(entity, raycastHit)
      }
      if(action == Symbols.selection) {
        val entity = values.getFirstValueFor(types.Entity)
        if (values.firstValueFor(lexiconTypes.Verb).toString == "Selection()") {
          selectEntity(entity)
        } else if (values.firstValueFor(lexiconTypes.Verb).toString == "Deselection()") {
          deselectEntity(entity)
        }
      }
      if (action == Symbols.entityDeletion) {
        val entity = values.getFirstValueFor(types.Entity)
        deleteEntity(entity)
      }
      if (action == Symbols.entityCreation) {
        val noun = values.getFirstValueFor(lexiconTypes.Noun)
        createEntity(noun.get.entityRelation.toString)
      }
      if (action == Symbols.color) {

        //TODO change method
        /*
        val entity = values.getFirstValueFor(types.Entity)
        val lexiconColor = values.getFirstValueFor(lexiconTypes.Adjective)
        if(lexiconColor.isDefined) {
          val color = lexiconColor.get.property.generate() //.value.toString
          colorEntity(entity, color)
        }*/

        ////// remove 1
        val entity = values.getFirstValueFor(types.Entity)
        val lexiconColor = values.getFirstValueFor(lexiconTypes.Adjective)
        println(lexiconColor)
        if (lexiconColor.isDefined) {
          val color = lexiconColor.get.value
          println("Lexicon Color: " + color)
          if (color.toString.equals("Green()")) {
            colorEntity(entity, "green")
          } else if (color.toString.equals("Blue()")) {
            colorEntity(entity, "blue")
          }
        }
        //////// remove 2
      }
    }
  }

  private def createEntity(name: String): Unit = {
    Update the properties of PrefabFactory `with` UnityComponent.createPrefabReference(name)
  }

  private def deleteEntity(e: Option[Entity]): Unit ={
    if(e.isDefined){
      e.get.remove()
    } else {
      println("[MMIParserApplication] Entity in deleteEntity() not defined")
    }
  }

  private def moveEntity(e: Option[Entity], raycastHit: Option[ConstVec3f]): Unit ={
    if(e.isDefined && raycastHit.isDefined){
      println("test")
      e.get.set(types.TargetPosition(raycastHit.get))
    } else {
      println("[MMIParserApplication] Entity or RayCastHit in moveEntity() not defined")
    }
  }

  private def selectEntity(e: Option[Entity]): Unit ={
    if (e.isDefined) {
      e.get.set(types.Selected(true))
    } else {
      println("[MMIParserApplication] Entity in selectEntity() not defined")
    }
  }

  private def deselectEntity(e: Option[Entity]): Unit = {
    if (e.isDefined) {
      e.get.set(types.Selected(false))
    } else {
      println("[MMIParserApplication] Entity in selectEntity() not defined")
    }
  }

  private def colorEntity(e: Option[Entity], name: String): Unit = {
    if (e.isDefined) {
      e.get.set(types.ColorName(name))
    } else {
      println("[MMIParserApplication] Entity in colorEntity() not defined")
    }
  }
  protected def removeFromLocalRep(e: Entity) {}

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: TimedRingBuffer.Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {}
}