package tests;

import static org.junit.Assert.*;
import model.TurboIssue;
import model.TurboMilestone;

import org.junit.Test;

import stubs.ModelStub;
import filter.ParseException;
import filter.Parser;
import filter.expression.Qualifier;


public class EvalTests {


    private final ModelStub model = new ModelStub();

    @Test
    public void id() {
    	TurboIssue issue = new TurboIssue("1", "desc", model);
    	issue.setId(1);

    	assertEquals(Qualifier.process(Parser.parse("id:1"), issue), true);
    	
    	// Non-number
    	assertEquals(Qualifier.process(Parser.parse("id:a"), issue), false);
    }

    /**
     * Tests for the presence of keywords in a particular issue.
     * @param issue
     */
    private void testForKeywords(TurboIssue issue) {
    	assertEquals(Qualifier.process(Parser.parse("test"), issue), true);

    	// Substring
    	assertEquals(Qualifier.process(Parser.parse("te"), issue), true);
    	
    	// Implicit conjunction
    	assertEquals(Qualifier.process(Parser.parse("is a"), issue), true);
    	
    	// Like above but out of order
    	assertEquals(Qualifier.process(Parser.parse("a is"), issue), true);    	
    }
    
    @Test
    public void keyword() {
    	TurboIssue issue = new TurboIssue("", "this is a test", model);
    	testForKeywords(issue);
    }
    
    @Test
    public void title() {
    	TurboIssue issue = new TurboIssue("this is a test", "", model);
    	testForKeywords(issue);
    }

    @Test
    public void body() {
    	TurboIssue issue = new TurboIssue("", "this is a test", model);
    	testForKeywords(issue);
    }

    @Test
    public void milestone() {
    	TurboMilestone milestone = new TurboMilestone("v1.0");
    	model.addMilestone(milestone);

    	TurboIssue issue = new TurboIssue("", "", model);
    	issue.setMilestone(milestone);

    	assertEquals(Qualifier.process(Parser.parse("milestone:v1.0"), issue), true);
    	assertEquals(Qualifier.process(Parser.parse("milestone:v1"), issue), true);
    	assertEquals(Qualifier.process(Parser.parse("milestone:1"), issue), false);
    	try {
        	assertEquals(Qualifier.process(Parser.parse("milestone:."), issue), true);
        	fail(". is not a valid token on its own");
    	} catch (ParseException e) {
    	}
    	assertEquals(Qualifier.process(Parser.parse("milestone:what"), issue), false);
    }

    @Test
    public void parent() {
    }

    @Test
    public void label() {
    }

    @Test
    public void assignee() {
    }
    
    @Test
    public void author() {
    }

    @Test
    public void involves() {
    }

    @Test
    public void state() {
    }

    @Test
    public void has() {
    }

    @Test
    public void no() {
    }

    @Test
    public void in() {
    }
    
    @Test
    public void type() {
    }

    @Test
    public void is() {
    }

    @Test
    public void created() {
    }

    @Test
    public void updated() {
    }

}
