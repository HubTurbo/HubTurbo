
package tests;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	DataManagerTests.class,
	FilterEvalTests.class,
	FilterParserTests.class
})

public class Tests {
}