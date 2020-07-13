package cg

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class EnabledFlopIfcUnitTester[ T <: EnabledFlopIfc](c: T) extends PeekPokeTester(c) {
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
  "foo" should "work" in {
     Driver(() => new EnabledFlop(), "verilator") {
       c => new EnabledFlopIfcUnitTester(c)
     } should be (true)
  }
}

class EnabledFlopUsingCGTest extends ChiselFlatSpec {
  "foo" should "work" in {
     Driver(() => new EnabledFlopUsingCG(), "verilator") {
       c => new EnabledFlopIfcUnitTester(c)
     } should be (true)
  }
}
