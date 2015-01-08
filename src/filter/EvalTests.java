package filter;

import static org.junit.Assert.assertEquals;
import model.TurboIssue;

import org.junit.Test;

import stubs.ModelStub;
import filter.expression.Qualifier;


public class EvalTests {

    @Test
    public void basics() {
    	ModelStub model = new ModelStub();
    	TurboIssue issue = new TurboIssue("1", "desc", model);
    	assertEquals(Qualifier.process(Parser.parse("id:1"), issue), false);
    }
}
