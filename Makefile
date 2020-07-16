.PHONY: test-enff test-enffcg test-cg2enff

test-enff:
	sbt 'test:runMain cg.EnabledFlopMain --backend-name verilator'

test-enffcg:
	sbt 'test:runMain cg.EnabledFlopUsingCGMain --backend-name verilator'

test-cg2enff:
	sbt 'test:runMain cg.EnabledFlopUsingCGMain --backend-name verilator -fct cg.tools.cg2enff'

test-cg2enff-hier:
	sbt 'test:runMain cg.EnabledFlopHierUsingCGMain --backend-name verilator --inline EnabledFlowHierUsingCG -fct cg.tools.cg2enff'
