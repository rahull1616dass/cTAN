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

import simx.applications.multimodalinteraction.io.{Servers, SpeechRecognizer}
import simx.components.ai.atn.misc.Bucketeer
import simx.components.editor.EditorComponentAspect
import simx.components.vrpn.VRPNComponentAspect
import simx.core.components.io.SpeechEvents
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}
import simx.core.entity.Entity
import simx.core.ontology.types
import simx.core.svaractor.SVarActor
import simx.core.worldinterface.eventhandling.{Event, EventHandler}

import scala.collection.immutable

object SpeechExample extends SimXApplicationMain[SpeechExample] {}

class SpeechExample(args : Array[String]) extends SimXApplication with EventHandler
{
  //Component names
  val editorName = 'editor
  val vrpnName = 'vrpn

  override protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    VRPNComponentAspect(vrpnName) and
    EditorComponentAspect(editorName, appName = "MasterControlProgram")

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]): Unit = {}

  protected def createEntities(): Unit = {}

  protected def finishConfiguration(): Unit = {
    println("[info][SpeechExample] Starting SpeechRecognizer")
    SVarActor.createActor(SpeechRecognizer(Servers.speechServer))

    SpeechEvents.hypothesis.observe{ event =>
      printSpeechEventToConsole(event, "hypothesized")
    }

    SpeechEvents.recognized.observe{ event =>
      printSpeechEventToConsole(event, "recognized")
    }
  }

  private def printSpeechEventToConsole(event: Event, _type: String): Unit ={
    println("[info][SpeechExample] Received "+_type+" token from SpeechRecognizer: " +
      event.values.firstValueFor(types.String) +
      " at " + event.values.firstValueFor(types.Time) +
      " with confidence: " + event.values.firstValueFor(types.Confidence))
  }

  override protected def removeFromLocalRep(e: Entity): Unit = {}
}
