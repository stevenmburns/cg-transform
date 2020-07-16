package cg

import chisel3.iotesters.Driver

object EnabledFlopMain extends App {
   Driver.execute(args, () => new EnabledFlop()) {
     c => new EnabledFlopIfcUnitTester(c)
   }
}

object EnabledFlopUsingCGMain extends App {
   Driver.execute(args, () => new EnabledFlopUsingCG()) {
     c => new EnabledFlopIfcUnitTester(c)
   }
}

object EnabledFlopHierUsingCGMain extends App {
   Driver.execute(args, () => new EnabledFlopHierUsingCG()) {
     c => new EnabledFlopIfcUnitTester(c)
   }
}