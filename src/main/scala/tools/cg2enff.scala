package cg.tools

import firrtl._
import firrtl.ir._
import firrtl.Mappers._

/*
import firrtl.options.{Dependency, PreservesAll}

class cg2enff extends Transform with DependencyAPIMigration with PreservesAll[Transform] {

  override def prerequisites = Seq.empty
  override def optionalPrerequisites = Seq.empty
  override def optionalPrerequisiteOf = Seq.empty
*/

class cg2enff extends Transform {

  val inputForm = LowForm
  val outputForm = HighForm

  def transformStmt(stmt: Statement): Statement = stmt match {
    case Connect(info, loc, expr) =>
      println( s"Found a connect: ${info} ${loc} ${expr}")
      PartialConnect(info, loc, expr)
    case s => s.map(transformStmt)
  }

  def transformMod(m: DefModule): DefModule = m.map(transformStmt)

  def execute(state: CircuitState): CircuitState = {
    val transformed = state.circuit.map(transformMod)
    state.copy(circuit = transformed)
  }
}
