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

package simx.applications.multimodalinteraction.mmiparser

import simx.components.ai.atn.ImplicitConversions._
import simx.components.ai.atn.core.{AtnProcessing, AugmentedTransitionNetwork, Condition, ConditionResult}
import simx.components.ai.atn.interaction.lexicon.WordTypes
import simx.components.ai.atn.ontology.{types => lexiconTypes}
import simx.components.ai.atn.{Events => AtnEvents}
import simx.components.ai.mipro.SemanticValueDSL
import simx.core.components.io.SpeechEvents
import simx.core.entity.Entity
import simx.core.entity.description.SValSet
import simx.core.ontology.functions.Interpolators
import simx.core.ontology.{Symbols, types => semanticTypes}
import simx.core.svaractor.TimedRingBuffer
import simx.core.svaractor.unifiedaccess.StateParticleInfo
import simx.core.worldinterface.entity.filter.{HasSVal, SValEquals}
import simx.core.worldinterface.eventhandling.{Event, EventDescription}


class MMIParser(aName: Symbol,
                  _drawGraphs: Boolean = true,
                  _autoResetAfter: Option[Long] = None
                 ) extends AtnProcessing with SemanticValueDSL{

  val EntitiesWithSemantic = HasSVal(semanticTypes.Semantics)
  val User = SValEquals(semanticTypes.Semantics(Symbols.user))

  Requires all properties from EntitiesWithSemantic

  private class ExampleAtn() extends AugmentedTransitionNetwork with MMIHelper with Interpolators
  {
    override val cursorMerge: Boolean = true
    override val inputTypes: List[EventDescription] = SpeechEvents.token :: SpeechEvents.recognized :: Nil
    override val outputTypes: List[EventDescription] = AtnEvents.command :: Nil

    create StartState 'startState withArc        'isVB            toTargetState 'hasVB
    create State      'hasVB      withSubArc     'isNP            toTargetState 'hasNP
    create State      'hasNP      withEpsilonArc 'firstCommandFinished toTargetState 'hasFirstCommand
    create State      'hasFirstCommand withArc 'isEx toTargetState 'hasEx //TODO//// withArc 'isAdj toTargetState 'hasAdj
    create State      'hasEx withEpsilonArc 'finalCommandFinished toTargetState 'endState
    create State      'hasAdj withEpsilonArc 'finalCommandFinished toTargetState 'endState
    create EndState 'endState

    create State    'isNP   withArc         'isDT       toTargetState 'hasDT
    create State 'hasDT withArc 'isNN toTargetState 'hasNN //TODO////withArc         'isAdj       toTargetState 'hasAdj
    //TODO////create State    'hasAdj  withArc         'isNN       toTargetState 'hasNN
    create State    'hasNN  withEpsilonArc  'resolveNP  toTargetState 'endNP
    create EndState 'endNP

    create Arc 'isVB            withCondition (checkForWordType[WordTypes.Verb], checkConfidence(0.2f))        addFunction copySpeechToRegisterAs(lexiconTypes.Verb)
    create Arc 'isDT            withCondition (checkForWordType[WordTypes.Determiner], checkConfidence(0.2f))  addFunction resolveDet
    create Arc 'isNN            withCondition (checkForWordType[WordTypes.Noun], checkConfidence(0.5f))        addFunction copySpeechToRegisterAs(lexiconTypes.Noun)
    create Arc 'isEx            withCondition (checkForWordType[WordTypes.Existential], checkConfidence(0.5f)) addFunction resolveEx
    create Arc 'isAdj           withCondition (checkForWordType[WordTypes.Adjective], checkConfidence(0.5f)) addFunction copySpeechToRegisterAs(lexiconTypes.Adjective)

    create Arc 'resolveNP             withCondition alwaysTrue                              addFunction resolveNP
    create Arc 'firstCommandFinished  withCondition alwaysTrue                              addFeedback returnCommand
    create Arc 'finalCommandFinished  withCondition alwaysTrue                              addFeedback returnCommand

    // a condition which always returns true
    private def alwaysTrue(in: Event, register: SValSet): Condition.Result = ConditionResult(doTransition = true)

    // resolves a recognized noun phrase
    private def resolveNP(in: Event, register: SValSet): Unit = {
      //TODO recognise Adjectives --> seperate Method to get all Objects with certain color
      val noun = register.firstValueFor(lexiconTypes.Noun)
      // retrieves all entities with a semantic property that corresponds to the noun from the application state
      val entities = Get all SValEquals(semanticTypes.Semantics(noun.entityRelation))
      // retrieves all activated entities from the cATNs register
      val activatedEntities = register.getAllValuesFor(semanticTypes.Entity)
      // performs multimodal fusion by comparing both lists
      val matchingEntities = entities.filter(activatedEntities.contains)
      if (matchingEntities.nonEmpty) {
        register.add(semanticTypes.Entity(matchingEntities.head))
      } else if (entities.nonEmpty) {
        register.add(semanticTypes.Entity(entities.head))
      }
    }

    // resolves the article/determiner "a", "the", "that"
    private def resolveDet(in: Event, register: SValSet): Unit = {
      //TODO Recognise object
      // retrieves the timestamp of the incoming speech event
      val detTimeStamp = in.values.firstValueFor(semanticTypes.Time)
      val allEntities = Get all HasSVal(semanticTypes.Activated)
      var activatedEntities: List[Entity] = Nil
      // checks which entities were activated at the detTimeStamp
      allEntities.foreach{e =>
        val wasActivated = (semanticTypes.Activated of e at detTimeStamp).value
        if(wasActivated) activatedEntities = e :: activatedEntities
      }

      /*
      val raycastHit = (semanticTypes.RaycastHit of User at detTimeStamp).value
      allEntities.foreach{e =>
        if(raycastHit.equals(semanticTypes.Position of e)) {
          activatedEntities = e :: activatedEntities
        }
      }*/
      activatedEntities.foreach{e => register.add(semanticTypes.Entity(e))}
    }

    // resolves the existential "there"
    private def resolveEx(in: Event, register: SValSet): Unit = {
      // retrieves the timestamp of the incoming speech event
      val exTimeStamp = in.values.firstValueFor(semanticTypes.Time)
      // checks where the user was pointing at exTimeStamp
      val raycastHit = (semanticTypes.RaycastHit of User at exTimeStamp).value
      register.add(semanticTypes.RaycastHit(raycastHit))
    }

    // sends an ATNEvent.command to the MMIParserApplication
    private def returnCommand(in: Event, register: SValSet): List[Event] = {
      AtnEvents.command(register.toSValSeq:_*) :: Nil
    }
    /*//resolves adjectives like color words
    private def resolveAdj(in: Event, register: SValSet): Unit = {
      // retrieves the timestamp of the incoming speech event
      val adjTimeStamp = in.values.firstValueFor(semanticTypes.Time)
      val colors = Get all HasSVal(semanticTypes.Color)
      // checks adjective
      //val adjWord = (semanticTypes.Color of Adjectives at adjTimeStamp).value
      colors.foreach{e =>
        val adjWord = (semanticTypes.Color of in.values at adjTimeStamp).value
        register.add(semanticTypes.Color(adjWord))
      }
      //register.add(semanticTypes.Color(adjWord))
    }*/
  }

  override protected def name: Symbol = aName

  override protected def drawGraphs: Boolean = _drawGraphs

  override protected def autoResetAfter: Option[IdType] = _autoResetAfter

  override protected def atn: AugmentedTransitionNetwork = new ExampleAtn()

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: TimedRingBuffer.Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {}

  override protected def removeFromLocalRep(e: Entity): Unit = {}
}
