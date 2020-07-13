package cg

import chisel3._
import chisel3.util._
//import chisel3.experimental._

class integrated_clock_gate extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clkinp = Input(Clock())
    val d = Input(Bool())
    val clkout = Output(Clock())
  })

  setResource("/integrated_clock_gate.v")
}

object integrated_clock_gate {
  def apply(clk: Clock, in: UInt) = {
    val p = Module (new integrated_clock_gate)
    p.io.clkinp := clk
    p.io.d := in
    p.io.clkout
  }
}

class EnabledFlopIfc(val Bits: Int) extends Module {
  val io = IO(new Bundle {
    val d = Input(UInt(Bits.W))
    val en = Input(Bool())
    val q = Output(UInt(Bits.W))
  })
}

class EnabledFlopUsingCG(Bits: Int = 1) extends EnabledFlopIfc(Bits) {
  val en_clk = integrated_clock_gate( clock, io.en)
  withClockAndReset( en_clk, reset) {
     io.q := RegNext( io.d)
  }
}

class EnabledFlop(Bits: Int = 1) extends EnabledFlopIfc(Bits) {
  io.q := RegEnable( next=io.d, enable=io.en)    
}

object enabled_flop_using_cg extends App {
  chisel3.Driver.execute(args, () => new EnabledFlopUsingCG())
}

object enabled_flop extends App {
  chisel3.Driver.execute(args, () => new EnabledFlop())
}
