package cg.tools

import firrtl._
import firrtl.stage.Forms
import firrtl.ir._
import firrtl.Mappers._

import firrtl.options.{Dependency, PreservesAll}

/*
class cg2enff extends Transform with DependencyAPIMigration {

  override def prerequisites = Forms.LowForm
  override def optionalPrerequisites = Seq.empty
  override def optionalPrerequisiteOf = Seq.empty

  override def invalidates(tx: Transform) = tx match {
    case _ : firrtl.transforms.DeadCodeElimination => true
    case _ => false
  }
*/

class cg2enff extends Transform {

  val inputForm = LowForm
  val outputForm = HighForm

  val instances = scala.collection.mutable.Map[String,String]()
  val enables = scala.collection.mutable.Map[String,Expression]()

  def is_reg_d( expr : Expression) : Boolean = expr match {
    case e @ WRef(name, tpe, kind, flow) =>
      kind == RegKind && flow == SinkFlow
    case _ =>
      false
  }

  def transformStmt(stmt: Statement): Statement = stmt match {
    case DefRegister( info, name, tpe, clock, reset, init) =>
      val bare_clock = WRef("clock",ClockType,PortKind,SourceFlow)
      DefRegister( info, name, tpe, bare_clock, reset, init)
    case Connect(info, loc, expr) if is_reg_d( loc) =>
      if ( enables contains "one") {
         val fexpr = loc match {
	    case e @ WRef(name, tpe, kind, flow) =>
	       WRef(name, tpe, kind, SourceFlow)
            case _ => expr
         }
         val new_expr = Mux( enables("one"), expr, fexpr)      
         Connect( info, loc, new_expr)
      } else {
         stmt
      }
    case s =>
      s map transformStmt
  }

  def transformMod(m: DefModule): DefModule = m.map(transformStmt)

  def find_enables( expr : Expression, name : String, tpe : Type, flow : Flow) : Boolean = {
     val is_integrated_clock_gate =
          expr match {
     	      case WRef(name, tpe, kind, flow) =>
	         kind == InstanceKind &&
	         (instances contains name) &&	         
	         instances(name) == "integrated_clock_gate"
              case _ =>
	         println( s"Shouldn't happen.")
	         false
          }
     is_integrated_clock_gate && name == "d" && flow == SinkFlow && tpe == UIntType(IntWidth(1))
  }

  def is_enable( expr : Expression) : Boolean = expr match {
    case WSubField(expr, name, tpe, flow) =>
      find_enables( expr, name, tpe, flow)
    case _ =>
      false
  }

  def gatherStmt(stmt: Statement) {
     stmt match {
        case WDefInstance(info, name, module, tpe) =>
          println( s"gatherStmt: found instance ${name} module ${module}")
          instances(name) = module
        case Connect(info, loc, expr) =>
	  if (is_enable( loc)) {
	     enables("one") = expr
	     println( s"gatherStmt: Driving enable: ${expr}")
          }
	case _ =>
     }
     stmt foreachStmt gatherStmt
  }

  def gatherMod(m: DefModule) : Unit = {
     m foreachStmt gatherStmt
  }

  def execute(state: CircuitState): CircuitState = {
    state.circuit foreachModule gatherMod
    println( s"Instances: ${instances}")    
    state.copy(circuit = state.circuit.map(transformMod))
  }
}
