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

package simx.applications.multimodalinteraction.atn

import simx.components.ai.atn.ImplicitConversions._
import simx.components.ai.atn.core.{ArcRep, Condition, ConditionResult, StateRep}
import simx.components.ai.atn.interaction.lexicon.{Lexicon, WordTypes}
import simx.core.components.io.SpeechEvents
import simx.core.entity.description.SValSet
import simx.core.entity.typeconversion.ConvertibleTrait
import simx.core.ontology.{Symbols, types}
import simx.core.worldinterface.eventhandling.Event

import scala.reflect.ClassTag

trait AtnHelper {

  protected def checkForWordType[T<: WordTypes.Word](in: Event)(implicit tag: ClassTag[T]): Condition.Result = {
    in.name match {
      case SpeechEvents.token.name | SpeechEvents.processedToken.name =>
        var isValid = false
        val token = in.values.firstValueFor(types.String)
        Lexicon.lookUp(token) match {
          case (Some(word @  (_:T))) => {
            isValid = true
          }
          case _ =>
        }
        ConditionResult(isValid)
      case _ => ConditionResult(doTransition = false)
    }
  }

  def copySpeechToRegisterAs[T <: WordTypes.Word](semanticType: ConvertibleTrait[T])(in: Event, register: SValSet, triggeredArc: ArcRep, curState: StateRep, prevState: StateRep): Unit = {
    in.values.getFirstValueFor(types.String).collect{case speechInput =>
      val word = Lexicon.lookUp(speechInput).get
      val sVal = semanticType.apply(word.asInstanceOf[semanticType.dataType])//(semanticType, word)//semanticType.apply(word)
      register.add(sVal)
      val tokens = register.getFirstValueFor(types.String.withAnnotations(Symbols.token))
      var newTokens = speechInput
      if(tokens.isDefined) newTokens = tokens.get + " " + newTokens
      register.add(types.String.withAnnotations(Symbols.token)(newTokens))
    }
  }
}
