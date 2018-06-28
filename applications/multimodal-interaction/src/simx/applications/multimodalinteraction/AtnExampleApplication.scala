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

import simx.applications.multimodalinteraction.atn.{ExampleAtnParser, ExampleWords}
import simx.applications.multimodalinteraction.io.SpeechSimulator
import simx.components.ai.atn.interaction.lexicon.Lexicon
import simx.components.ai.atn.ontology.{types => lexiconTypes}
import simx.components.ai.atn.{Events => AtnEvents}
import simx.components.editor.EditorComponentAspect
import simx.components.synchronization.unity.UnityComponentAspect
import simx.core.entity.Entity
import simx.core.ontology.{EntityDescription, Symbols, types}
import simx.core.svaractor.SVarActor
import simx.core.svaractor.SVarActor.Ref
import simx.core.worldinterface.eventhandling.EventHandler
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}



object AtnExampleApplication extends SimXApplicationMain (new AtnExampleApplication) {}


class AtnExampleApplication extends SimXApplication with EventHandler
{

  val useUnity = false

  protected def applicationConfiguration: ApplicationConfig = ApplicationConfig withComponent
    EditorComponentAspect('editor) and
    UnityComponentAspect('unity, "localhost", 8000) iff useUnity



  protected def configureComponents(components: Map[Symbol, Ref]): Unit = {
    SVarActor.createActor(new ExampleAtnParser('myATN))
    SVarActor.createActor(SpeechSimulator())

    Lexicon.clear()
    Lexicon.put("select", ExampleWords.Selection())
    Lexicon.put("a", ExampleWords.Article())
    Lexicon.put("ball", ExampleWords.Ball())
    Lexicon.put("box", ExampleWords.Box())
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
    }
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
}