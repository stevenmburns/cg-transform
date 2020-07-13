.PHONY: test-enff test-enffcg test-cg2enff

test-enff:
	sbt 'test:runMain cg.EnabledFlopMain'

test-enffcg:
	sbt 'test:runMain cg.EnabledFlopUsingCGMain'

test-cg2enff:
	sbt 'test:runMain cg.EnabledFlopUsingCGMain -fct cg.tools.cg2enff'


