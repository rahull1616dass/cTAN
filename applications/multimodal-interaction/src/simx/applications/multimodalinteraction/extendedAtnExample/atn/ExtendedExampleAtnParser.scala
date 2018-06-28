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

package simx.applications.multimodalinteraction.extendedAtnExample.atn

import simx.applications.multimodalinteraction.atn.AtnHelper
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


class ExtendedExampleAtnParser(aName: Symbol,
                               _drawGraphs: Boolean = true,
                               _autoResetAfter: Option[Long] = None
                 ) extends AtnProcessing with SemanticValueDSL{

  val EntitiesWithSemantic = HasSVal(semanticTypes.Semantics)
  val User = SValEquals(semanticTypes.Semantics(Symbols.user))

  Requires all properties from EntitiesWithSemantic

  private class ExtendedExampleAtn() extends AugmentedTransitionNetwork with AtnHelper with Interpolators
  {
    override val cursorMerge: Boolean = true
    override val inputTypes: List[EventDescription] = SpeechEvents.token :: Nil
    override val outputTypes: List[EventDescription] = AtnEvents.command :: Nil

    create StartState 'startState       withArc        'isVB                 toTargetState 'hasVB
    create State      'hasVB            withSubArc     'isNP                 toTargetState 'hasNP
    create State      'hasNP            withEpsilonArc 'firstCommandFinished toTargetState 'hasFirstCommand
    (create State     'hasFirstCommand  withArc        'isADJ                toTargetState 'hasADJ
                                        withArc        'isEx                 toTargetState 'hasEx)
    create State      'hasEx            withEpsilonArc 'commandFinished      toTargetState 'endState
    create State      'hasADJ           withEpsilonArc 'commandFinished      toTargetState 'endState
    create EndState   'endState

    create State    'isNP   withArc         'isDT       toTargetState 'hasDT
    (create State   'hasDT  withArc         'isNN       toTargetState 'hasNN
                            withArc         'isADJ      toTargetState 'hasDT)
    create State    'hasNN  withEpsilonArc  'resolveNP  toTargetState 'endNP
    create EndState 'endNP

    create Arc 'isVB                 withCondition checkForWordType[WordTypes.Verb]        addFunction  copySpeechToRegisterAs(lexiconTypes.Verb)
    create Arc 'isADJ                withCondition checkForWordType[WordTypes.Adjective]   addFunction  copySpeechToRegisterAs(lexiconTypes.Adjective)
    create Arc 'isEx                 withCondition checkForWordType[WordTypes.Existential] addFunction (copySpeechToRegisterAs(lexiconTypes.Existential), resolveEx)
    create Arc 'isDT                 withCondition checkForWordType[WordTypes.Determiner]  addFunction  copySpeechToRegisterAs(lexiconTypes.Determiner)
    create Arc 'isNN                 withCondition checkForWordType[WordTypes.Noun]        addFunction  copySpeechToRegisterAs(lexiconTypes.Noun)
    create Arc 'resolveNP            withCondition alwaysTrue                              addFunction  resolveNP
    create Arc 'firstCommandFinished withCondition alwaysTrue                              addFeedback  returnFirstCommand
    create Arc 'commandFinished      withCondition alwaysTrue                              addFeedback  returnCommand

    private def alwaysTrue(in: Event, register: SValSet): Condition.Result = ConditionResult(doTransition = true)

    private def resolveNP(in: Event, register: SValSet): Unit = {
      val noun = register.firstValueFor(lexiconTypes.Noun)
      var entities = Get all SValEquals(semanticTypes.Semantics(noun.entityRelation))
      val adj = register.getFirstValueFor(lexiconTypes.Adjective)
      if(adj.isDefined){
        val property = adj.get.property
        val semanticValueScale =  property.generate().asInstanceOf[semanticTypes.Scale]
        if(semanticValueScale.value.x > 0) {
          entities = entities.sortWith{(e1, e2) =>
            val scaleE1 = (semanticTypes.Scale of e1).value
            val scaleE2 = (semanticTypes.Scale of e2).value
            scaleE1.x >= scaleE2.x
          }
        }
      }

      if(entities.nonEmpty) register.add(semanticTypes.Entity(entities.head))
    }

    private def resolveEx(in: Event, register: SValSet): Unit = {
      val exTimeStamp = in.values.firstValueFor(semanticTypes.Time)
      val raycastHit = (semanticTypes.RaycastHit of User at exTimeStamp).value
      register.add(semanticTypes.RaycastHit(raycastHit))
    }

    private def returnFirstCommand(in: Event, register: SValSet): List[Event] = {
      val entity = register.firstSValFor(semanticTypes.Entity)
      val noun = register.firstSValFor(lexiconTypes.Noun)
      val action = register.firstSValFor(lexiconTypes.Verb)
      AtnEvents.command(entity, action, noun) :: Nil
    }

    private def returnCommand(in: Event, register: SValSet): List[Event] = {
      val entity = register.firstSValFor(semanticTypes.Entity)
      val action = register.firstSValFor(lexiconTypes.Verb)
      val adj = register.getFirstSValFor(lexiconTypes.Adjective)
      val ex = register.getFirstSValFor(lexiconTypes.Existential)
      val raycastHit = register.getFirstSValFor(semanticTypes.RaycastHit)
      if(ex.isDefined){
        AtnEvents.command(action, entity, ex.get, raycastHit.get) :: AtnEvents.reset(semanticTypes.Identifier(name)) :: Nil
      } else if(adj.isDefined) {
        AtnEvents.command(action, entity, adj.get) :: AtnEvents.reset(semanticTypes.Identifier(name)) :: Nil
      } else {
        Nil
      }
    }
  }

  override protected def name: Symbol = aName

  override protected def drawGraphs: Boolean = _drawGraphs

  override protected def autoResetAfter: Option[IdType] = _autoResetAfter

  override protected def atn: AugmentedTransitionNetwork = new ExtendedExampleAtn()

  override def onNewRequirementValue(e: Entity, requirementInfo: StateParticleInfo[_], timestamp: TimedRingBuffer.Time): Unit = {}

  override def onStartOfObservation(e: Entity, requirementInfo: StateParticleInfo[_]): Unit = {}

  override protected def removeFromLocalRep(e: Entity): Unit = {}
}
