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

package simx.applications.multimodalinteraction.io

import simx.components.vrpn.devices.VRPNToken
import simx.core.components.io.SpeechEvents
import simx.core.entity.Entity
import simx.core.entity.component.EntityCreationHandling
import simx.core.entity.description.SValSet
import simx.core.helper.{Hypothesized, Recognized}
import simx.core.ontology.{Symbols, types}
import simx.core.svaractor.SVarActor
import simx.core.worldinterface.eventhandling.EventProvider

case class SpeechRecognizer(ip: String) extends SVarActor with EventProvider with EntityCreationHandling {

  private val EPToken = SValSet(types.EntityType(Symbols.token))

  /**
    * Called when the actor is started
    * Observes the entity which contains the speech recognition results
    * and emits them as SpeechEvents
    */
  override protected def startUp(): Unit = {
    VRPNToken("Tracker0@" + ip).desc.realize(println)

    onOneEntityAppearance(EPToken.toFilter){ tokenEntity =>
      tokenEntity.observe(types.Token){ newToken =>
        newToken match {
          case Recognized(text: String, confidence: Float, timestamp: Long) if !text.contains(" ") =>
            emitSpeechEvent(text,confidence,timestamp)
          case Hypothesized(text: String, confidence: Float, timestamp: Long) if !text.contains(" ") =>
           emitSpeechEvent(text,confidence,timestamp, hypothesized = true)
          case _ =>
        }
      }
    }
  }

  /**
    * Emits a SpeechEvent from the recognized data
    * @param text The text which has been recognized from the speech recognition software
    * @param confidence The confidence value of the recognized speech
    * @param timestamp The timestamp of the recognized speech
    * @param hypothesized A boolean which indicates if the recognition is hypothesized or recognized
    */
  def emitSpeechEvent(text: String, confidence: Float, timestamp: Long, hypothesized: Boolean = false): Unit ={
    if(hypothesized){
      SpeechEvents.hypothesis.emit(types.String(text), types.Time(timestamp), types.Confidence(confidence))
    } else {
      println(s"$text=$confidence")
      SpeechEvents.recognized.emit(types.String(text), types.Time(timestamp), types.Confidence(confidence))
    }
    //SpeechEvents.token.emit(types.String(text), types.Time(timestamp), types.Confidence(confidence))
  }

  /**
    * Called when an entity gets removed
    * @param e The entity that gets removed
    */
  override protected def removeFromLocalRep(e: Entity): Unit = {}
}
