package tests;

import static filter.expression.QualifierType.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import filter.ParseException;
import filter.Parser;
import filter.expression.*;

public class FilterParserTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parse_nullInput_exceptionThrown() {
        thrown.expect(NullPointerException.class);
        Parser.parse(null);
    }

    @Test
    public void parse_emptyInput_emptyQualifier() {
        assertEquals(Parser.parse(""), Qualifier.EMPTY);
    }

    @Test
    public void parse_basicExamples_validAST() {
        assertEquals(Parser.parse("a(b)"),
                     new Conjunction(new Qualifier(KEYWORD, "a"), new Qualifier(KEYWORD, "b")));
        assertEquals(Parser.parse("    a   (   b   )   "),
                     new Conjunction(new Qualifier(KEYWORD, "a"), new Qualifier(KEYWORD, "b")));
        assertEquals(Parser.parse("a(b c)"),
                     new Conjunction(
                             new Qualifier(KEYWORD, "a"),
                             new Conjunction(new Qualifier(KEYWORD, "b"), new Qualifier(KEYWORD, "c"))));
        assertEquals(Parser.parse("c a(b)"),
                     new Conjunction(
                             new Conjunction(
                                     new Qualifier(KEYWORD, "c"),
                                     new Qualifier(KEYWORD, "a")),
                             new Qualifier(KEYWORD, "b")));
        assertNotEquals(new Qualifier(EMPTY, ""), null);
        assertNotEquals(new Qualifier(EMPTY, ""), "null");
        assertNotEquals(new Qualifier(KEYWORD, "a"), new Qualifier(KEYWORD, "b"));
        assertNotEquals(new Qualifier(EMPTY, "a"), new Qualifier(KEYWORD, "a"));
    }

    @Test
    public void parse_quotedInputs_validAST() {
        // Content with space
        assertEquals(Parser.parse("body:\"this is\""), new Qualifier(DESCRIPTION, "this is"));

        // Symbol between quotes
        assertEquals(Parser.parse("\":\""), new Qualifier(KEYWORD, ":"));

    }

    @Test
    public void parse_emptyQuotes_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("body: \"\"");
    }

    @Test
    public void parse_unbalancedQuotes_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("body: \"\"\"");
    }

    @Test
    public void parse_quotesInPrefixPosition_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("body: test\"will not be parsed\"\"");
    }

    @Test
    public void parse_unrecognisedTokenAsQualifierContent_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("a:.");
    }

    @Test
    public void parse_noQualifierContent_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("a:");
    }

    @Test
    public void parse_standalonePrefixOperator_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("~");
    }

    @Test
    public void parse_standaloneInfixOperator_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("&&");
    }

    @Test
    public void parse_standaloneSemicolon_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse(";");
    }

    @Test
    public void parse_nothingAfterInfixOperator_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("a(b) ||");
    }

    @Test
    public void parse_precedingEmptyQualifer_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("-milestone: label:priority.high");
    }

    @Test
    public void parse_trailingEmptyQualifer_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("label:priority.high -milestone:");
    }

    @Test
    public void parse_missingSemicolonOperand_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("a:b; c:d;e");
    }

    @Test
    public void parse_unexpectedTokenAtEOF_exceptionThrown() {
        thrown.expect(ParseException.class);
        Parser.parse("sort:a,");
    }

    @Test
    public void parse_disjunction_validAST() {
        assertEquals(Parser.parse("id:b OR id:d"),
                     new Disjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
        assertEquals(Parser.parse("id:b | id:d"),
                     new Disjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
        assertEquals(Parser.parse("id:b || id:d"),
                     new Disjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
    }

    @Test
    public void parse_conjunction_validAST() {
        assertEquals(Parser.parse("id:b id:d"),
                     new Conjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
        assertEquals(Parser.parse("id:b AND id:d"),
                     new Conjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
        assertEquals(Parser.parse("id:b & id:d"),
                     new Conjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
        assertEquals(Parser.parse("id:b && id:d"),
                     new Conjunction(new Qualifier(ID, "b"), new Qualifier(ID, "d")));
    }

    @Test
    public void parse_negation_validAST() {
        assertEquals(Parser.parse("!id:b"),
                     new Negation(new Qualifier(ID, "b")));
        assertEquals(Parser.parse("-id:b"),
                     new Negation(new Qualifier(ID, "b")));
        assertEquals(Parser.parse("~id:b"),
                     new Negation(new Qualifier(ID, "b")));
    }

    @Test
    public void parse_disjunctionShorthand_validAST() {

        assertEquals(Parser.parse("label:a;b;c"),
                     new Disjunction(
                             new Disjunction(
                                     new Qualifier(LABEL, "a"),
                                     new Qualifier(LABEL, "b")),
                             new Qualifier(LABEL, "c")));

        assertEquals(Parser.parse("~label:a;b"),
                     new Negation(
                             new Disjunction(
                                     new Qualifier(LABEL, "a"),
                                     new Qualifier(LABEL, "b"))));
    }

    @Test
    public void parse_disjunctionShorthandInSort_exceptionThrown() {
        // TODO: change this test in the future when proper semantic exception is implemented #1173
        // It currently does not throw an exception.
        Parser.parse("sort:label;assignee");
    }

    @Test
    public void parse_implicitConjunction_correctAST() {
        assertEquals(Parser.parse("milestone:0.4 state:open OR label:urgent"),
                     new Disjunction(
                             new Conjunction(
                                     new Qualifier(MILESTONE, "0.4"),
                                     new Qualifier(STATE, "open")),
                             new Qualifier(LABEL, "urgent")));
        assertEquals(Parser.parse("milestone:0.4 state:open OR label:urgent"),
                     Parser.parse("milestone:0.4 AND state:open OR label:urgent"));
    }

    @Test
    public void parse_associativity_correctAST() {
        assertEquals(Parser.parse("id:b OR id:d OR id:f"),
                     new Disjunction(
                             new Disjunction(
                                     new Qualifier(ID, "b"),
                                     new Qualifier(ID, "d")),
                             new Qualifier(ID, "f")));

        assertEquals(Parser.parse("id:b AND id:d AND id:f"),
                     new Conjunction(
                             new Conjunction(
                                     new Qualifier(ID, "b"),
                                     new Qualifier(ID, "d")),
                             new Qualifier(ID, "f")));
    }

    @Test
    public void parse_numberOperators_validAST() {
        assertEquals(Parser.parse("updated:<24"),
                     new Qualifier(UPDATED, new NumberRange(null, 24, true)));
        assertEquals(Parser.parse("updated:<=24"),
                     new Qualifier(UPDATED, new NumberRange(null, 24)));
        assertEquals(Parser.parse("updated:>=24"),
                     new Qualifier(UPDATED, new NumberRange(24, null)));
        assertEquals(Parser.parse("updated:>24"),
                     new Qualifier(UPDATED, new NumberRange(24, null, true)));
        assertNotEquals(Parser.parse("updated:<24"),
                        new Qualifier(UPDATED, new NumberRange(24, null, true)));

        assertEquals(Parser.parse("updated:24"),
                     new Qualifier(UPDATED, 24));
        assertNotEquals(Parser.parse("updated:25"),
                        new Qualifier(UPDATED, 24));
    }

    @Test
    public void parse_numberRanges_validAST() {
        assertEquals(Parser.parse("updated:1 .. 24"),
                     new Qualifier(UPDATED, new NumberRange(1, 24, false)));
        assertEquals(Parser.parse("updated:1 .. *"),
                     new Qualifier(UPDATED, new NumberRange(1, null)));

        // Parsing currently requires a space between operand and operator
        assertEquals(Parser.parse("updated:1..2"),
                     new Qualifier(UPDATED, "1..2"));
    }

    @Test
    public void parse_dateRanges_validAST() {
        assertEquals(Parser.parse("created:2014-06-01 .. 2013-03-15"),
                     new Qualifier(CREATED, new DateRange(LocalDate.of(2014, 6, 1), LocalDate.of(2013, 3, 15))));

        assertEquals(Parser.parse("created:2014-06-01 .. *"),
                     new Qualifier(CREATED, new DateRange(LocalDate.of(2014, 6, 1), null)));

        assertEquals(Parser.parse("a created:2014-06-01 .. 2013-03-15 b"),
                     new Conjunction(
                             new Conjunction(
                                     new Qualifier(KEYWORD, "a"),
                                     new Qualifier(CREATED,
                                                   new DateRange(LocalDate.of(2014, 6, 1),
                                                                 LocalDate.of(2013, 3, 15)))),
                             new Qualifier(KEYWORD, "b"))
        );
    }

    @Test
    public void encloses_dateRanges_correctEnclosure() {

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
    public void parse_dateOperators_validAST() {
        assertEquals(Parser.parse("created:<2014-06-01"),
                     new Qualifier(CREATED, new DateRange(null, LocalDate.of(2014, 6, 1), true)));

        assertEquals(Parser.parse("created : <= 2014-06-01"),
                     new Qualifier(CREATED, new DateRange(null, LocalDate.of(2014, 6, 1))));

        assertEquals(Parser.parse("created : > 2014-06-01"),
                     new Qualifier(CREATED, new DateRange(LocalDate.of(2014, 6, 1), null, true)));

        assertEquals(Parser.parse("created : >= 2014-06-01"),
                     new Qualifier(CREATED, new DateRange(LocalDate.of(2014, 6, 1), null)));

        assertNotEquals(Parser.parse("created : <= 2014-06-01"),
                        new Qualifier(CREATED, new DateRange(LocalDate.of(2014, 6, 1), null)));
    }

    @Test
    public void parse_dates_validAST() {
        assertEquals(Parser.parse("created   :   2014-6-1"),
                     new Qualifier(CREATED, LocalDate.of(2014, 6, 1)));

        assertEquals(Parser.parse("created   :   2014-06-01"),
                     new Qualifier(CREATED, LocalDate.of(2014, 6, 1)));

        assertEquals(Parser.parse("a created   :   2014-06-01 b"),
                     new Conjunction(
                             new Conjunction(
                                     new Qualifier(KEYWORD, "a"),
                                     new Qualifier(CREATED, LocalDate.of(2014, 6, 1))),
                             new Qualifier(KEYWORD, "b")));

        assertNotEquals(new Qualifier(CREATED, LocalDate.of(2014, 6, 2)),
                        new Qualifier(CREATED, LocalDate.of(2014, 6, 1)));
    }

    @Test
    public void parse_precedence_correctAST() {
        assertEquals(Parser.parse("id:b OR id:d AND id:f"),
                     new Disjunction(
                             new Qualifier(ID, "b"),
                             new Conjunction(
                                     new Qualifier(ID, "d"),
                                     new Qualifier(ID, "f"))));
        assertEquals(Parser.parse("~id:b OR id:d AND id:f"),
                     new Disjunction(
                             new Negation(
                                     new Qualifier(ID, "b")),
                             new Conjunction(new Qualifier(ID, "d"), new Qualifier(ID, "f"))));

        assertEquals(Parser.parse("id:b ~id:d"),
                     new Conjunction(new Qualifier(ID, "b"), new Negation(new Qualifier(ID, "d"))));
    }

    @Test
    public void parse_grouping_correctAST() {
        assertEquals(Parser.parse("(id:b OR id:d) AND id:f"),
                     new Conjunction(
                             new Disjunction(
                                     new Qualifier(ID, "b"),
                                     new Qualifier(ID, "d")),
                             new Qualifier(ID, "f")));
        assertEquals(Parser.parse("(id:b OR id:d) id:f"),
                     new Conjunction(
                             new Disjunction(
                                     new Qualifier(ID, "b"),
                                     new Qualifier(ID, "d")),
                             new Qualifier(ID, "f")));
        assertEquals(Parser.parse("id:f ~(id:b OR id:d)"),
                     new Conjunction(
                             new Qualifier(ID, "f"),
                             new Negation(
                                     new Disjunction(
                                             new Qualifier(ID, "b"),
                                             new Qualifier(ID, "d")))));
    }

    @Test
    public void parse_whitespaceAroundColons_validAST() {
        assertEquals(Parser.parse("assignee:darius"),
                     new Qualifier(ASSIGNEE, "darius"));
        assertEquals(Parser.parse("assignee    :    darius   "),
                     new Qualifier(ASSIGNEE, "darius"));
        assertEquals(Parser.parse("assignee:dar ius(one)"),
                     new Conjunction(
                             new Conjunction(
                                     new Qualifier(ASSIGNEE, "dar"),
                                     new Qualifier(KEYWORD, "ius")),
                             new Qualifier(KEYWORD, "one")));
    }

    @Test
    public void parse_sortingKeys_validAST() {
        assertEquals(Parser.parse("sort:id"),
                     new Qualifier(SORT, Arrays.asList(new SortKey("id", false))));
        assertEquals(Parser.parse("sort: id , repo "),
                     new Qualifier(SORT, Arrays.asList(new SortKey("id", false), new SortKey("repo", false))));

        assertEquals(Parser.parse("sort:-id"),
                     new Qualifier(SORT, Arrays.asList(new SortKey("id", true))));
        assertEquals(Parser.parse("sort:id, ~repo"),
                     new Qualifier(SORT, Arrays.asList(new SortKey("id", false), new SortKey("repo", true))));
        assertEquals(Parser.parse("sort:~id, NOT repo"),
                     new Qualifier(SORT, Arrays.asList(new SortKey("id", true), new SortKey("repo", true))));
        assertNotEquals(Parser.parse("sort:~repo, NOT id"),
                        new Qualifier(SORT, Arrays.asList(new SortKey("id", true), new SortKey("repo", true))));
    }

    @Test
    public void parse_serialisation_idempotent() {

        String[] tests = {
            "", // Empty
            "aa", // Keywords
            "aa bb cc",
            "\"a b\"",
            "abcdefg:hijkl",
            "!abcdefg:hijkl", // NOT
            "abcdefg:hijkl || zxc:aksljd", // OR
            "abcdefg:hijkl && zxc:aksljd", // AND
            "abcdefg:hijkl && zxc:aksljd || alsk:asl", // OR and AND precedence
            "abcdefg:hijkl || zxc:aksljd && alsk:asl",
            "(abcdefg:hijkl || zxc:aksljd) && alsk:asl", // Grouping
            "(abcdefg:hijkl && zxc:aksljd) || alsk:asl",
            "abcdefg:hijkl && !zxc:aksljd || !alsk:asl", // OR, AND, NOT
            "abcdefg:hijkl || !zxc:aksljd && !alsk:asl",
            "updated:>24", // Number ranges
            "updated:>=24",
            "updated:<24",
            "updated:<=24",
            "updated:24",
            "created:<=2014-12-4", // Date operators
            "created:>=2014-12-4",
            "created:<2014-12-4",
            "created:>2014-12-4",
            "created:2014-12-4",
            "created:2014-12-4 .. 2014-12-6", // Date ranges
            "sort:id", // Sorting keys
            "sort:id,repo",
            "sort:~id,repo",
            "sort:~id,!repo"
        };

        // We want to ensure that parsing some filter and parsing the serialised version
        // of that filter result in the same data structure.
        for (String test : tests) {
            assert test != null;
            FilterExpression a = Parser.parse(Parser.parse(test).toString());
            FilterExpression b = Parser.parse(test);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }
    }


    @Test
    public void check_incompleteValidInputs_true() {

        // Incomplete but valid inputs are accepted when the parser checks syntax
        assertTrue(Parser.check(""));
        assertTrue(Parser.check("id:"));
        assertTrue(Parser.check("id: a ||"));
        assertTrue(Parser.check("("));
        assertTrue(Parser.check("id: a ("));
    }

    @Test
    public void check_validInputs_true() {

        // Valid inputs are still accepted
        assertTrue(Parser.check("id:c && (id:a || id:b)"));
    }

    @Test
    public void check_invalidInputs_false() {

        // Ill-formed inputs are still rejected
        assertFalse(Parser.check("id: id:"));
        assertFalse(Parser.check("id: ("));
    }
}
