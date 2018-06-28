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

import simplex3d.math.floatx.Vec3f
import simx.components.ai.atn.interaction.lexicon.WordTypes
import simx.components.ai.atn.misc.SimpleSemanticTypeInstance
import simx.core.ontology.{Symbols, types}
import simx.core.ontology.types.OntologySymbol

object ExtendedExampleWords {

  case class Ball() extends WordTypes.Noun {
    val entityRelation: OntologySymbol = Symbols.ball
  }

  case class Creation() extends WordTypes.Verb {
    val actions: Set[OntologySymbol] = Set(Symbols.entityCreation)
  }

  case class Box() extends WordTypes.Noun {
    val entityRelation: OntologySymbol = Symbols.box
  }

  case class Selection() extends WordTypes.Verb {
    val actions: Set[OntologySymbol] = Set(Symbols.selection)
  }

  case class Translation() extends WordTypes.Verb {
    val actions: Set[OntologySymbol] = Set(Symbols.move)
  }

  case class Scale() extends WordTypes.Verb {
    val actions: Set[OntologySymbol] = Set(Symbols.scale)
  }

  case class Article() extends WordTypes.Determiner

  case class Bigger() extends WordTypes.Adjective {
    val property = new SimpleSemanticTypeInstance(types.Scale(Vec3f(2f, 2f, 2f)))
  }

  case class Big() extends WordTypes.Adjective {
    val property = new SimpleSemanticTypeInstance(types.Scale(Vec3f(1f, 1f, 1f)))
  }

  case class Existential() extends WordTypes.Existential

}
