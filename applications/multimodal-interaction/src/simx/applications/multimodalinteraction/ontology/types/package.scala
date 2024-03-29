package simx.applications.multimodalinteraction.ontology

/**
 * This file is automatically generated from an ontology.
 * DO NOT EDIT!
 */

import simx.applications.multimodalinteraction.ontology.functions.Functions
import simx.core.ontology.types._
import simx.core.entity.description.SVal
import simx.core.entity.description.SValHistory

package object types{
  def init(){}
	object Visible extends simx.core.ontology.SValDescription[scala.Boolean,scala.Boolean,simx.core.svaractor.semantictrait.base.Base,simx.core.ontology.Symbols.visible.SymbolType](simx.core.ontology.types.NullType as simx.core.ontology.Symbols.visible withType classOf[scala.Boolean] definedAt "http://www.hci.uni-wuerzburg.de/ontologies/simx/applications/multimodalinteraction/mmi.owl#Visible") with simx.core.ontology.types.SemanticSValType[Visible] {
		override def apply(value: dataType): SemanticSValType = new Visible(value, -1L, Nil)
		override def apply(value: dataType, timestamp : scala.Long): SemanticSValType = new Visible(value, timestamp, Nil)
		override def apply(value: dataType, timestamp : scala.Long, history: simx.core.entity.description.HistoryStorage.HistoryType[scala.Boolean]) = new VisibleWithHistory(value, timestamp, history)
	}
	class Visible(private val _value : Visible.dataType, private val _timestamp: scala.Long, private val _history: simx.core.entity.description.HistoryStorage.HistoryType[scala.Boolean]) extends simx.core.entity.description.SVal[scala.Boolean,simx.core.entity.typeconversion.TypeInfo[scala.Boolean,scala.Boolean],simx.core.svaractor.semantictrait.base.Base,simx.core.ontology.Symbols.visible.SymbolType](_value, Visible.valueDescription, Visible, _timestamp, _history) {
		def withHistory = new VisibleWithHistory(value, timestamp, history)
	}
	class VisibleWithHistory(_v: scala.Boolean, _t: scala.Long, _h: simx.core.entity.description.HistoryStorage.HistoryType[scala.Boolean]) extends Visible(_v,_t,_h) with SValHistory[scala.Boolean,simx.core.ontology.Symbols.visible.SymbolType,Visible] {
		def newNonHistoryInstance(value: scala.Boolean) = Visible(value)
	}
}