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

  val instances = scala.collection.mutable.Map[String,String]()

  val enables = scala.collection.mutable.Map[String,Expression]()

/*
Found a WDefInstance( @[latch.scala 19:20],integrated_clock_gate,integrated_clock_gate,BundleType(ArrayBuffer(Field(clkinp,Flip,ClockType), Field(d,Flip,UIntType(IntWidth(1))), Field(clkout,Default,ClockType))))
Found a WSubField(WRef(integrated_clock_gate,BundleType(ArrayBuffer(Field(clkinp,Flip,ClockType), Field(d,Flip,UIntType(IntWidth(1))), Field(clkout,Default,ClockType))),InstanceKind,SourceFlow),clkout,ClockType,SourceFlow)
Found a WSubField(WRef(integrated_clock_gate,BundleType(ArrayBuffer(Field(clkinp,Flip,ClockType), Field(d,Flip,UIntType(IntWidth(1))), Field(clkout,Default,ClockType))),InstanceKind,SourceFlow),clkinp,ClockType,SinkFlow)
Found a WSubField(WRef(integrated_clock_gate,BundleType(ArrayBuffer(Field(clkinp,Flip,ClockType), Field(d,Flip,UIntType(IntWidth(1))), Field(clkout,Default,ClockType))),InstanceKind,SourceFlow),d,UIntType(IntWidth(1)),SinkFlow)
Instances: Map(integrated_clock_gate -> integrated_clock_gate)
*/

  def find_enables( expr : Expression, name : String, tpe : Type, flow : Flow) : Boolean = {
     val is_integrated_clock_gate =
          expr match {
     	      case e @ WRef(name, tpe, kind, flow) =>
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
    case e @ WSubField(expr, name, tpe, flow) =>
      find_enables( expr, name, tpe, flow)
    case _ =>
      false
  }

  def is_reg_d( expr : Expression) : Boolean = expr match {
    case e @ WRef(name, tpe, kind, flow) =>
      kind == RegKind && flow == SinkFlow
    case _ =>
      false
  }

  def transformExpr(expr: Expression): Expression = expr match {
    case e @ WRef(name, tpe, kind, flow) =>
      //println( s"Found a ${e}")
      e map transformExpr
    case e @ WSubField(expr, name, tpe, flow) if find_enables( expr, name, tpe, flow) =>
      //println( s"Found a ${e}")
      e map transformExpr
    case e =>
      //println( s"Fallthrough Expression: found a ${e}")
      e map transformExpr
  }

  def transformStmt(stmt: Statement): Statement = stmt match {
    case s @ DefRegister( info, name, tpe, clock, reset, init) =>
      val bare_clock = WRef("clock",ClockType,PortKind,SourceFlow)
      DefRegister( info, name, tpe, bare_clock, reset, init)
    case s @ Connect(info, loc, expr) if is_reg_d( loc) =>
      println( s"Register d: ${expr}")
      //println( s"Found a ${s}")
      if ( enables contains "one") {
         val fexpr = loc match {
	    case e @ WRef(name, tpe, kind, flow) => WRef(name, tpe, kind, SourceFlow)
            case _ => expr
         }
         val new_expr = Mux( enables("one"), expr, fexpr)      
         Connect( info, loc, new_expr)
      } else {
         s
      }
    case s =>
      //println( s"Fallthrough Statement: found a ${s}")
      s map transformExpr map transformStmt
  }

  def transformMod(m: DefModule): DefModule = m.map(transformStmt)

  def gatherExpr(expr: Expression) {
     //println( s"gatherExpr ${expr}")
     expr foreachExpr gatherExpr
  }

  def gatherStmt(stmt: Statement) {
     //println( s"gatherStmt ${stmt}")
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
     stmt foreachExpr gatherExpr
     stmt foreachStmt gatherStmt

  }

  def gatherMod(m: DefModule) : Unit = {
     //println( s"gatherMod ${m}")
     m foreachStmt gatherStmt
  }

  def execute(state: CircuitState): CircuitState = {
    state.circuit.foreachModule(gatherMod)
    println( s"Instances: ${instances}")    
    state.copy(circuit = state.circuit.map(transformMod))
  }
}
