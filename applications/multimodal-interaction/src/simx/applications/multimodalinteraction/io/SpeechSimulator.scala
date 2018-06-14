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

import simx.core.components.io.SpeechEvents
import simx.core.ontology.types
import simx.core.svaractor.SVarActor
import simx.core.worldinterface.eventhandling.EventProvider

case class SpeechSimulator() extends SVarActor with EventProvider {

  /**
    * called when the actor is started
    */
  override protected def startUp(): Unit = {
    waitForInput()
  }

  addHandler[SpeechInput]{ input =>
    SpeechEvents.token.emit(types.String(input.text), types.Time(input.timestamp), types.Confidence(input.confidence))
    waitForInput()
  }

  def waitForInput(): Unit = {
    println("Type a word:")
    val input = scala.io.StdIn.readLine()
    self ! SpeechInput(input, System.currentTimeMillis())
  }
}

case class SpeechInput(text: String, timestamp: Long, confidence: Float = 0.9f)
