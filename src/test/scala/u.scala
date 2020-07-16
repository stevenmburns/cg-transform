package cg

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class EnabledFlopIfcUnitTester[ T <: EnabledFlopIfc](c: T) extends PeekPokeTester(c) {
  var pairs = for { i <- 0 until 100} yield {
     val d = BigInt( c.Bits, rnd)
     val en = BigInt( 1, rnd)
     (en,d)
  }

  var outval = Option.empty[BigInt]
  for { (en,d) <- pairs} {
     if ( en == 1) {
        outval = Some(d)
     }
     poke( c.io.en, en)
     poke( c.io.d, d)
     step(1)
     for { q <- outval} {
       expect( c.io.q, q)
     }
  }

  poke( c.io.en, 1)
  poke( c.io.d, 0)
  step(1)
  expect( c.io.q, 0)

  poke( c.io.en, 1)
  poke( c.io.d, 1)
  step(1)
  expect( c.io.q, 1)

  poke( c.io.en, 0)
  poke( c.io.d, 1)
  step(1)
  expect( c.io.q, 1)

  poke( c.io.en, 0)
  poke( c.io.d, 0)
  step(1)
  expect( c.io.q, 1)
}

class EnabledFlopTest extends ChiselFlatSpec {
  "EnabledFlop" should "behave like an enabled flop" in {
     Driver(() => new EnabledFlop(), "verilator") {
       c => new EnabledFlopIfcUnitTester(c)
     } should be (true)
 }
}

class EnabledFlopUsingCGTest extends ChiselFlatSpec {
  "EnabledFlopUsingCG" should "behave like an enabled flop" in {
     Driver(() => new EnabledFlopUsingCG(), "verilator") {
       c => new EnabledFlopIfcUnitTester(c)
     } should be (true)
  }
}

class EnabledFlopHierUsingCGTest extends ChiselFlatSpec {
  "EnabledFlopHierUsingCG" should "behave like an enabled flop" in {
     Driver(() => new EnabledFlopHierUsingCG(), "verilator") {
       c => new EnabledFlopIfcUnitTester(c)
     } should be (true)
  }
}
