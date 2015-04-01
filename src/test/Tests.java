
package test;

import model.Model;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import service.ServiceManager;
import test.DataManagerTests;
import test.EventTests;
import test.FilterEvalTests;
import test.FilterParserTests;
import test.ModelTests;
import test.ServiceManagerTests;
import test.TickingTimerTests;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	DataManagerTests.class,
	FilterEvalTests.class,
	FilterParserTests.class,
	EventTests.class,
	ServiceManagerTests.class,
	ModelTests.class,
	TickingTimerTests.class
})

public class Tests {
	@BeforeClass
	public static void setup() {
		ServiceManager.isInTestMode = true;
		Model.isInTestMode = true;
	}
}