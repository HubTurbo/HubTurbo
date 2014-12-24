package filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import filter.expression.Conjunction;
import filter.expression.DateRange;
import filter.expression.Disjunction;
import filter.expression.Negation;
import filter.expression.Predicate;
import filter.lexer.Lexer;
import filter.lexer.Token;
import filter.lexer.TokenType;

public class Tests {

    @Test
    public void basics() {
        assertEquals(Parser.parse(null), null);
        assertEquals(Parser.parse(""), Predicate.EMPTY);
    }
    
    @Test
    public void keywords() {
        assertEquals(Parser.parse("a(b)"), new Conjunction(new Predicate("keyword", "a"), new Predicate("keyword", "b")));
        assertEquals(Parser.parse("    a   (   b   )   "), new Conjunction(new Predicate("keyword", "a"), new Predicate("keyword", "b")));
        assertEquals(Parser.parse("a(b c)"), new Conjunction(new Predicate("keyword", "a"), new Conjunction(new Predicate("keyword", "b"), new Predicate("keyword", "c"))));
        assertEquals(Parser.parse("c a(b)"), new Conjunction(new Conjunction(new Predicate("keyword", "c"), new Predicate("keyword", "a")), new Predicate("keyword", "b")));
    }
    
    @Test
    public void quotes() {
    	// Quoted qualifier content
    	assertEquals(Parser.parse("created:\"a b\""), new Predicate("created", "a b"));
    	assertEquals(Parser.parse("created:\" > 2014-5-1 \""), new Predicate("created", new DateRange(LocalDate.of(2014, 5, 1), null, true)));
    	assertEquals(Parser.parse("created:\" 2014-5-1 .. 2014-5-2 \""), new Predicate("created", new DateRange(LocalDate.of(2014, 5, 1), LocalDate.of(2014, 5, 2))));
    	
    	// Prefix quotes
    	assertEquals(Parser.parse("\"a b\""), new Predicate("keyword", "a b"));
    }
    
    @Test
    public void unexpectedEOFs() {
        try {
            Parser.parse("~");
            fail("Inputs which end unexpectedly should throw a parse exception");
        } catch (ParseException e) {}
        try {
            Parser.parse("a(b) ||");
            fail("Inputs which end unexpectedly should throw a parse exception");
        } catch (ParseException e) {}
    }
    
    @Test
    public void operators() {
        assertEquals(Parser.parse("a:b OR c:d"), new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")));
        assertEquals(Parser.parse("a:b | c:d"), new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")));
        assertEquals(Parser.parse("a:b || c:d"), new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")));

        assertEquals(Parser.parse("a:b c:d"), new Conjunction(new Predicate("a", "b"), new Predicate("c", "d")));
        assertEquals(Parser.parse("a:b AND c:d"), new Conjunction(new Predicate("a", "b"), new Predicate("c", "d")));
        assertEquals(Parser.parse("a:b & c:d"), new Conjunction(new Predicate("a", "b"), new Predicate("c", "d")));
        assertEquals(Parser.parse("a:b && c:d"), new Conjunction(new Predicate("a", "b"), new Predicate("c", "d")));

        assertEquals(Parser.parse("!a:b"), new Negation(new Predicate("a", "b")));
        assertEquals(Parser.parse("-a:b"), new Negation(new Predicate("a", "b")));
        assertEquals(Parser.parse("~a:b"), new Negation(new Predicate("a", "b")));
        
        // Implicit conjunction
        
        assertEquals(Parser.parse("milestone:0.4 state:open OR label:urgent"),
                new Disjunction(new Conjunction(new Predicate("milestone", "0.4"), new Predicate("state", "open")), new Predicate("label", "urgent")));
        assertEquals(Parser.parse("milestone:0.4 state:open OR label:urgent"), Parser.parse("milestone:0.4 AND state:open OR label:urgent"));
    }
    
    @Test
    public void associativity() {
        assertEquals(Parser.parse("a:b OR c:d OR e:f"),
                new Disjunction(new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")), new Predicate("e", "f")));

        assertEquals(Parser.parse("a:b AND c:d AND e:f"),
                new Conjunction(new Conjunction(new Predicate("a", "b"), new Predicate("c", "d")), new Predicate("e", "f")));
    }

    @Test
    public void dateRanges() {
        assertEquals(Parser.parse("created:2014-06-01 .. 2013-03-15"),
                new Predicate("created", new DateRange(LocalDate.of(2014, 06, 01), LocalDate.of(2013, 03, 15))));

        assertEquals(Parser.parse("created:2014-06-01 .. *"),
                new Predicate("created", new DateRange(LocalDate.of(2014, 06, 01), null)));

        assertEquals(Parser.parse("a created:2014-06-01 .. 2013-03-15 b"),
        		new Conjunction(
        				new Conjunction(
        						new Predicate("keyword", "a"),
        						new Predicate("created", new DateRange(LocalDate.of(2014, 06, 01), LocalDate.of(2013, 03, 15)))),
        				new Predicate("keyword", "b"))
        );
    }

    @Test
    public void dateRangeUsage() {
    	
    	// date .. date
    	DateRange dr = new DateRange(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 6, 3));
    	assertEquals(dr.encloses(LocalDate.of(2014, 1, 1)), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 6, 3)), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 2, 2)), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 7, 1)), false);

    	// date .. date
    	// strict inequality
    	dr = new DateRange(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 6, 3), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 1, 1)), false);
    	assertEquals(dr.encloses(LocalDate.of(2014, 6, 3)), false);
    	assertEquals(dr.encloses(LocalDate.of(2014, 2, 2)), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 7, 1)), false);

    	// date .. *
    	dr = new DateRange(LocalDate.of(2014, 1, 1), null);
    	assertEquals(dr.encloses(LocalDate.of(2014, 1, 1)), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 6, 3)), true);
    	assertEquals(dr.encloses(LocalDate.of(2013, 2, 2)), false);

    	// date .. *
    	// strict inequality
    	dr = new DateRange(LocalDate.of(2014, 1, 1), null, true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 1, 1)), false);
    	assertEquals(dr.encloses(LocalDate.of(2014, 6, 3)), true);
    	assertEquals(dr.encloses(LocalDate.of(2013, 2, 2)), false);

    	// * .. date
    	dr = new DateRange(null, LocalDate.of(2014, 1, 1));
    	assertEquals(dr.encloses(LocalDate.of(2014, 1, 1)), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 6, 3)), false);
    	assertEquals(dr.encloses(LocalDate.of(2013, 2, 2)), true);

    	// * .. date
    	// strict inequality
    	dr = new DateRange(null, LocalDate.of(2014, 1, 1), true);
    	assertEquals(dr.encloses(LocalDate.of(2014, 1, 1)), false);
    	assertEquals(dr.encloses(LocalDate.of(2014, 6, 3)), false);
    	assertEquals(dr.encloses(LocalDate.of(2013, 2, 2)), true);
    }
    
    @Test
    public void dateRangeOperators() {
        assertEquals(Parser.parse("created:<2014-06-01"),
                new Predicate("created", new DateRange(null, LocalDate.of(2014, 6, 1), true)));
        
        assertEquals(Parser.parse("created : <= 2014-06-01"),
                new Predicate("created", new DateRange(null, LocalDate.of(2014, 6, 1))));

        assertEquals(Parser.parse("created : > 2014-06-01"),
                new Predicate("created", new DateRange(LocalDate.of(2014, 6, 1), null, true)));
        
        assertEquals(Parser.parse("created : >= 2014-06-01"),
                new Predicate("created", new DateRange(LocalDate.of(2014, 6, 1), null)));
    }
    
    @Test
    public void dates() {
        assertEquals(Parser.parse("created   :   2014-6-1"),
                new Predicate("created", LocalDate.of(2014, 6, 1)));

        assertEquals(Parser.parse("created   :   2014-06-01"),
                new Predicate("created", LocalDate.of(2014, 6, 1)));
        
        assertEquals(Parser.parse("a created   :   2014-06-01 b"),
        		new Conjunction(
        				new Conjunction(
        						new Predicate("keyword", "a"),
        						new Predicate("created", LocalDate.of(2014, 06, 01))),
						new Predicate("keyword", "b")));
    }

    @Test
    public void precedence() {
        assertEquals(Parser.parse("a:b OR c:d AND e:f"),
                new Disjunction(new Predicate("a", "b"), new Conjunction(new Predicate("c", "d"), new Predicate("e", "f"))));
        assertEquals(Parser.parse("~a:b OR c:d AND e:f"),
                new Disjunction(new Negation(new Predicate("a", "b")), new Conjunction(new Predicate("c", "d"), new Predicate("e", "f"))));
        
        assertEquals(Parser.parse("a:b ~c:d"), new Conjunction(new Predicate("a", "b"), new Negation(new Predicate("c", "d"))));
    }

    @Test
    public void grouping() {
        assertEquals(Parser.parse("(a:b OR c:d) AND e:f"),
                new Conjunction(new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")), new Predicate("e", "f")));
        assertEquals(Parser.parse("(a:b OR c:d) e:f"),
                new Conjunction(new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")), new Predicate("e", "f")));
        assertEquals(Parser.parse("e:f ~(a:b OR c:d)"),
                new Conjunction(new Predicate("e", "f"), new Negation(new Disjunction(new Predicate("a", "b"), new Predicate("c", "d")))));
    }
    
    @Test
    public void colon() {
        assertEquals(Parser.parse("assignee:darius"),
                new Predicate("assignee", "darius"));
        assertEquals(Parser.parse("assignee    :    darius   "),
                new Predicate("assignee", "darius"));
        assertEquals(Parser.parse("assignee:dar ius(one)"),
                new Conjunction(new Conjunction(new Predicate("assignee", "dar"), new Predicate("keyword", "ius")), new Predicate("keyword", "one")));
    }
    
    @Test
    public void lexer() {
        assertEquals(new Lexer("").lex(), new ArrayList<Token>(Arrays.asList(
                new Token(TokenType.EOF, "", 0))));
        assertEquals(new Lexer("a' b' c'").lex(), new ArrayList<Token>(Arrays.asList(
                new Token(TokenType.SYMBOL, "a'", 0),
                new Token(TokenType.SYMBOL, "b'", 0),
                new Token(TokenType.SYMBOL, "c'", 0),
                new Token(TokenType.EOF, "", 0))));
    }
}
