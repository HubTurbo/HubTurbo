
package tests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import service.ServiceManager;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	DataManagerTests.class,
	FilterEvalTests.class,
	FilterParserTests.class,
	EventTests.class,
	ModelTests.class
})

public class Tests {
	@BeforeClass
	public static void setup() {
		ServiceManager.isTestMode = true;
	}
}