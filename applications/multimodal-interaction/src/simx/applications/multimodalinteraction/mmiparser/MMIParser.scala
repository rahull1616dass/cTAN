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
    create State      'hasVB      withSubArc     'isNP            toTargetState 'hasNP //TODO: Add another arc in the cATN parallel to the 'isNP subarc
    create State      'hasNP      withEpsilonArc 'firstCommandFinished toTargetState 'endState  //TODO: Extend the ATN to recognize the existential "there"
    create EndState 'endState

    create State    'isNP   withArc         'isDT       toTargetState 'hasDT
    create State    'hasDT  withArc         'isNN       toTargetState 'hasNN
    create State    'hasNN  withEpsilonArc  'resolveNP  toTargetState 'endNP
    create EndState 'endNP

    create Arc 'isVB            withCondition (checkForWordType[WordTypes.Verb], checkConfidence(0.2f))        addFunction copySpeechToRegisterAs(lexiconTypes.Verb)
    create Arc 'isDT            withCondition (checkForWordType[WordTypes.Determiner], checkConfidence(0.2f))  addFunction resolveDet
    create Arc 'isNN            withCondition (checkForWordType[WordTypes.Noun], checkConfidence(0.5f))        addFunction copySpeechToRegisterAs(lexiconTypes.Noun)
    create Arc 'isEx            withCondition (checkForWordType[WordTypes.Existential], checkConfidence(0.5f)) addFunction resolveEx

    create Arc 'resolveNP             withCondition alwaysTrue                              addFunction resolveNP
    create Arc 'firstCommandFinished  withCondition alwaysTrue                              addFeedback returnCommand
    create Arc 'finalCommandFinished  withCondition alwaysTrue                              addFeedback returnCommand

    // a condition which always returns true
    private def alwaysTrue(in: Event, register: SValSet): Condition.Result = ConditionResult(doTransition = true)

    // resolves a recognized noun phrase
    private def resolveNP(in: Event, register: SValSet): Unit = {
      val noun = register.firstValueFor(lexiconTypes.Noun)
      // retrieves all entities with a semantic property that corresponds to the noun from the application state
      val entities = Get all SValEquals(semanticTypes.Semantics(noun.entityRelation))
      // retrieves all selected entities from the cATNs register
      val selectedEntities = register.getAllValuesFor(semanticTypes.Entity)
      // performs multimodal fusion by comparing both lists
      val matchingEntities = entities.filter(selectedEntities.contains)
      if (matchingEntities.nonEmpty) {
        register.add(semanticTypes.Entity(matchingEntities.head))
      } else if (entities.nonEmpty) {
        register.add(semanticTypes.Entity(entities.head))
      }
    }

    // resolves the pronoun "myself"
    private def resolvePro(in: Event, register: SValSet): Unit = {
      // gets the user from the application state
      val user = Get one User
      // adds the user entity to the register
      register.add(semanticTypes.Entity(user))
    }

    // resolves the artivle/determiner "a", "the", "that"
    private def resolveDet(in: Event, register: SValSet): Unit = {
      // retrieves the timestamp of the incoming speech event
      val detTimeStamp = in.values.firstValueFor(semanticTypes.Time)
      val allEntities = Get all HasSVal(semanticTypes.Selected)
      var selectedEntities: List[Entity] = Nil
      // checks which entities were selected at the detTimeStamp
      allEntities.foreach{e =>
        val wasSelected = (semanticTypes.Selected of e at detTimeStamp).value
        if(wasSelected) selectedEntities = e :: selectedEntities
      }
      selectedEntities.foreach{e => register.add(semanticTypes.Entity(e))}
    }

    // resolves the existental "there"
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
  }

  override protected def name: Symbol = aName

  override protected def drawGraphs: Boolean = _drawGraphs

  override protected def autoResetAfter: Option[IdType] = _autoResetAfter

  override protected def atn: AugmentedTransitionNetwork = new ExampleAtn()

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: TimedRingBuffer.Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {}

  override protected def removeFromLocalRep(e: Entity): Unit = {}
}
