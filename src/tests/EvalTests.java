package tests;

import static org.junit.Assert.assertEquals;
import model.TurboIssue;

import org.junit.Test;

import stubs.ModelStub;
import filter.Parser;
import filter.expression.Qualifier;


public class EvalTests {


    private final ModelStub model = new ModelStub();

    @Test
    public void id() {
    	TurboIssue issue = new TurboIssue("1", "desc", model);
    	issue.setId(1);

    	assertEquals(Qualifier.process(Parser.parse("id:1"), issue), true);
    	assertEquals(Qualifier.process(Parser.parse("id:a"), issue), false);
    }

    @Test
    public void keyword() {
    }
    
    @Test
    public void title() {
    }

    @Test
    public void body() {
    }

    @Test
    public void milestone() {
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
