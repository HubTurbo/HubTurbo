package tests;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.ParseException;
import filter.Parser;
import filter.SemanticException;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import filter.expression.QualifierType;
import prefs.Preferences;

import static filter.expression.Qualifier.USER_WARNING_ERROR_FORMAT;

public class FilterEvalTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final IModel empty;
    public static final String REPO = "test/test";

    public FilterEvalTests() {
        empty = new MultiModel(mock(Preferences.class));
        empty.setDefaultRepo(REPO);
    }

    @Test
    public void checksInvalidQualifier() {
        TurboIssue issue = new TurboIssue(REPO, 1, "title");
        assertFalse(matches("something:a", issue));
    }

    @Test
    public void satisfiesFalseQualifier() {
        TurboIssue issue = new TurboIssue(REPO, 1, "1");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        IModel model = TestUtils.modelWith(issue, milestone);

        assertFalse(Qualifier.process(model, Qualifier.FALSE, issue));
    }

    @Test
    public void satisfiesId_validInputs() {
        TurboIssue issue1 = new TurboIssue(REPO, 1, "1");

        assertTrue(matches("id:1", issue1));
        assertTrue(matches("id:>=1", issue1));
        assertTrue(matches("id:<=1", issue1));
        assertFalse(matches("id:<1", issue1));
        assertFalse(matches("id:>1", issue1));
        assertFalse(matches("id:2", issue1));

        assertTrue(matches("id:<2", issue1));
        assertTrue(matches("id:<=2", issue1));
        assertTrue(matches("id:>0", issue1));
        assertTrue(matches("id:>=0", issue1));
    }

    @Test
    public void satisfiesId_validCompoundId() {
        TurboIssue issue = new TurboIssue("dummy/dummy", 1, "1");

        assertFalse(matches("id:test/test#1", issue));
        assertFalse(matches("id:dummy/dummy#2", issue));
        assertTrue(matches("id:dummy/dummy#1", issue));
    }

    @Test
    public void satisfiesId_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.ID, "id:something");
    }

    @Test
    public void satisfiesId_compoundIdWithRangeOperator() {
        TurboIssue issue = new TurboIssue("dummy/dummy", 4, "4");

        assertTrue(matches("id:dummy/dummy#>3", issue));
        assertTrue(matches("id:dummy/dummy#>=4", issue));
        assertTrue(matches("id:dummy/dummy#<6", issue));
        assertTrue(matches("id:dummy/dummy#<=6", issue));
        assertTrue(matches("id:dummy/dummy#3 .. 6", issue));
    }

    @Test
    public void satisfiesId_invalidCompoundIdInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.ID, "id:test/test#something");
    }

    private void testForPresenceOfKeywords(String prefix, TurboIssue issue) {

        // Exact match
        assertTrue(matches(prefix + "test", issue));

        // Substring
        assertTrue(matches(prefix + "te", issue));

        // Implicit conjunction
        assertTrue(matches(prefix + "is a", issue));

        // Like above but out of order
        assertTrue(matches(prefix + "a is", issue));
    }

    @Test
    public void satisfiesTitle_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "this is a test");
        testForPresenceOfKeywords("title:", issue);
    }

    @Test
    public void satisfiesBody_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setDescription("this is a test");
        testForPresenceOfKeywords("body:", issue);
        testForPresenceOfKeywords("desc:", issue);
        testForPresenceOfKeywords("description:", issue);
    }

    @Test
    public void satisfiesIn_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setDescription("this is a test");
        testForPresenceOfKeywords("in:body ", issue);

        issue = new TurboIssue(REPO, 1, "this is a test");
        testForPresenceOfKeywords("in:title ", issue);
        testForPresenceOfKeywords("in:t ", issue);
    }

    @Test
    public void satisfiesIn_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.IN, "in:something test");
    }

    private void testMilestoneParsing(String milestoneQualifier, TurboIssue issue, IModel model) {
        assertTrue(Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "v1.0"), issue));
        assertTrue(Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "v1"), issue));
        assertTrue(Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "v"), issue));
        assertFalse(Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "1"), issue));

        try {
            assertTrue(Qualifier.process(model, Parser.parse(milestoneQualifier + ":."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }
        assertFalse(matches(milestoneQualifier + ":what", issue));
    }

    @Test
    public void satisfiesMilestone_validInputs() {
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setMilestone(milestone);

        IModel model = TestUtils.modelWith(issue, milestone);

        testMilestoneParsing("milestone", issue, model);
        // test: qualifier alias
        testMilestoneParsing("m", issue, model);

        // milestone aliases
        // - sorting of milestone is by due date
        // - test different offset

        // test: overdue open milestone with no open issues would not be current milestone
        TurboMilestone msCurrMin3 = new TurboMilestone(REPO, 10, "V0.1");
        msCurrMin3.setOpen(true);
        msCurrMin3.setOpenIssues(0);
        msCurrMin3.setDueDate(Optional.of(LocalDate.now().minusMonths(2)));
        TurboIssue iCurrMin3 = new TurboIssue(REPO, 2, "curr-3");
        iCurrMin3.setMilestone(msCurrMin3);
        iCurrMin3.setOpen(false);

        // test: sort by due date is correct
        TurboMilestone msCurrMin2 = new TurboMilestone(REPO, 9, "V0.2");
        msCurrMin2.setOpen(false);
        msCurrMin2.setDueDate(Optional.of(LocalDate.now().minusMonths(1)));
        TurboIssue iCurrMin2 = new TurboIssue(REPO, 3, "curr-2");
        iCurrMin2.setMilestone(msCurrMin2);

        // test: future closed milestone will not be current milestone
        TurboMilestone msCurrMin1 = new TurboMilestone(REPO, 8, "V0.3");
        msCurrMin1.setOpen(false);
        msCurrMin1.setDueDate(Optional.of(LocalDate.now().plusDays(1)));
        TurboIssue iCurrMin1 = new TurboIssue(REPO, 4, "curr-1");
        iCurrMin1.setMilestone(msCurrMin1);

        // test: earliest future open milestone with 0 open issues will
        // be current milestone
        TurboMilestone msCurr = new TurboMilestone(REPO, 7, "V0.5");
        msCurr.setOpen(true);
        msCurr.setOpenIssues(0);
        msCurr.setDueDate(Optional.of(LocalDate.now().plusMonths(1)));
        TurboIssue iCurr = new TurboIssue(REPO, 5, "curr");
        iCurr.setMilestone(msCurr);
        iCurr.setOpen(false);

        // test: sort by due date is correct, even if in the future but
        // closed
        TurboMilestone msCurrPlus1 = new TurboMilestone(REPO, 6, "V0.7");
        msCurrPlus1.setOpen(false);
        msCurrPlus1.setDueDate(Optional.of(LocalDate.now().plusMonths(2)));
        TurboIssue iCurrPlus1 = new TurboIssue(REPO, 6, "curr+1");
        iCurrPlus1.setMilestone(msCurrPlus1);

        // test: sort by due date is correct
        TurboMilestone msCurrPlus2 = new TurboMilestone(REPO, 5, "V0.8");
        msCurrPlus2.setOpen(true);
        msCurrPlus2.setDueDate(Optional.of(LocalDate.now().plusMonths(3)));
        TurboIssue iCurrPlus2 = new TurboIssue(REPO, 7, "curr+2");
        iCurrPlus2.setMilestone(msCurrPlus2);

        // test: milestone with no due date should not be included
        TurboMilestone msCurrPlus3 = new TurboMilestone(REPO, 4, "V0.9");
        msCurrPlus3.setDueDate(Optional.empty());
        TurboIssue iCurrPlus3 = new TurboIssue(REPO, 8, "curr+3");
        iCurrPlus3.setMilestone(msCurrPlus3);

        model = TestUtils.singletonModel(new Model(REPO,
                new ArrayList<>(Arrays.asList(iCurrMin3, iCurrMin2, iCurrMin1,
                        iCurr, iCurrPlus1, iCurrPlus2, iCurrPlus3)),
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(msCurrMin3, msCurrMin2, msCurrMin1,
                        msCurr, msCurrPlus1, msCurrPlus2, msCurrPlus3)),
                new ArrayList<>()));

        FilterExpression noMilestoneAlias;

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-3"));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-2"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-1"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+1"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+2"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+3"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        // test: negation alias
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("-milestone:curr"));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertTrue(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        // test: no milestone in model should return qualifier false
        model = TestUtils.singletonModel(new Model(REPO,
                                                   new ArrayList<>(Arrays.asList(iCurrMin3, iCurrMin2, iCurrMin1,
                                                                                 iCurr, iCurrPlus1, iCurrPlus2,
                                                                                 iCurrPlus3)),
                                                   new ArrayList<>(),
                                                   new ArrayList<>(),
                                                   new ArrayList<>()));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-3"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-2"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-1"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+1"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+2"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+3"));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurr));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertFalse(Qualifier.process(model, noMilestoneAlias, iCurrPlus3));
    }

    @Test
    public void satisfiesLabel_validInputs() {
        TurboLabel label = new TurboLabel(REPO, "type.bug");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.addLabel(label);

        IModel model = TestUtils.modelWith(issue, label);

        assertFalse(Qualifier.process(model, Parser.parse("label:type"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("label:type."), issue));
        assertTrue(Qualifier.process(model, Parser.parse("label:type.bug"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("label:bug"), issue));
        try {
            assertTrue(Qualifier.process(model, Parser.parse("label:.bug"), issue));
            fail(". cannot begin symbols");
        } catch (ParseException ignored) {
        }
        try {
            assertFalse(Qualifier.process(model, Parser.parse("label:."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }

        // Ensures that an issue isn't rejected by a qualifier just because it has some label
        // that the qualifier doesn't express, i.e. it should only be rejected if it does not
        // have any label that the qualifier expresses. See Qualifier#labelsSatisfy for details.

        TurboLabel label2 = new TurboLabel(REPO, "something");
        issue = new TurboIssue(REPO, 1, "");
        issue.addLabel(label2);
        issue.addLabel(label);

        model = TestUtils.singletonModel(new Model(new Model(REPO,
                                                             new ArrayList<>(Arrays.asList(issue)),
                                                             new ArrayList<>(Arrays.asList(label, label2)),
                                                             new ArrayList<>(),
                                                             new ArrayList<>())));

        assertTrue(Qualifier.process(model, Parser.parse("label:t."), issue));

        // Label without a group

        label = new TurboLabel(REPO, "bug");

        issue = new TurboIssue(REPO, 1, "");
        issue.addLabel(label);

        model = TestUtils.modelWith(issue, label);

        assertFalse(Qualifier.process(model, Parser.parse("label:bug."), issue));
        assertFalse(Qualifier.process(model, Parser.parse("label:type.bug"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("label:type"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("label:bug"), issue));
        try {
            assertTrue(Qualifier.process(model, Parser.parse("label:.bug"), issue));
            fail(". cannot begin symbols");
        } catch (ParseException ignored) {
        }
        try {
            assertFalse(Qualifier.process(model, Parser.parse("label:."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }
    }

    @Test
    public void satisfiesAssignee_validInputs() {
        TurboUser user = new TurboUser(REPO, "bob", "alice");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setAssignee(user);

        IModel model = TestUtils.modelWith(issue, user);

        assertTrue(Qualifier.process(model, Parser.parse("assignee:BOB"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("assignee:bob"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("assignee:alice"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("assignee:o"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("assignee:lic"), issue));

        // test: qualifier alias
        assertTrue(Qualifier.process(model, Parser.parse("as:BOB"), issue));
    }

    @Test
    public void satisfiesAuthor_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "bob", null, false);
        IModel model = TestUtils.modelWith(issue, new TurboUser("test/test", "bob"));

        assertTrue(Qualifier.process(model, Parser.parse("author:BOB"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("author:bob"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("author:o"), issue));

        // test: qualifier alias
        assertTrue(Qualifier.process(model, Parser.parse("au:bob"), issue));
    }

    @Test
    public void satisfiesInvolves_validInput() {
        // involves = assignee || author

        // assignee
        TurboUser user = new TurboUser(REPO, "bob", "alice");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setAssignee(user);

        IModel model = TestUtils.modelWith(issue, user);

        assertTrue(Qualifier.process(model, Parser.parse("involves:BOB"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("involves:bob"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("involves:alice"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("involves:o"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("involves:lic"), issue));

        // test: qualifier alias
        assertTrue(Qualifier.process(model, Parser.parse("user:BOB"), issue));

        // author
        issue = new TurboIssue(REPO, 1, "", "bob", null, false);

        assertTrue(Qualifier.process(model, Parser.parse("involves:BOB"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("involves:bob"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("involves:alice"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("involves:o"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("involves:lic"), issue));
    }

    @Test
    public void satisfiesState_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setOpen(false);
        assertFalse(matches("state:open", issue));
        assertFalse(matches("state:o", issue));
        assertTrue(matches("state:closed", issue));
        assertTrue(matches("state:c", issue));

        // test: qualifier alias
        assertFalse(matches("st:open", issue));
    }

    @Test
    public void satisfiesState_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.STATE, "state:something");
        verifyQualifierContentError(QualifierType.STATE, "state:1");
    }

    @Test
    public void satisfiesHasConditions_validInputs() {
        TurboLabel label = new TurboLabel(REPO, "type.bug");
        TurboUser user = new TurboUser(REPO, "bob", "alice");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertFalse(matches("has:label", issue));
        assertFalse(matches("has:milestone", issue));
        assertFalse(matches("has:assignee", issue));

        // test: qualifier alias
        assertFalse(matches("h:label", issue));

        // test: keyword aliases  
        assertFalse(matches("has:l", issue));
        assertFalse(matches("has:labels", issue));
        assertFalse(matches("has:milestones", issue));
        assertFalse(matches("has:m", issue));
        assertFalse(matches("has:as", issue));

        issue.addLabel(label);
        IModel model = TestUtils.modelWith(issue, label);

        assertTrue(Qualifier.process(model, Parser.parse("has:label"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("has:assignee"), issue));

        issue.setMilestone(milestone);
        model = TestUtils.modelWith(issue, label, milestone);

        assertTrue(Qualifier.process(model, Parser.parse("has:label"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("has:assignee"), issue));

        issue.setAssignee(user);
        model = TestUtils.modelWith(issue, label, milestone, user);

        assertTrue(Qualifier.process(model, Parser.parse("has:label"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("has:assignee"), issue));
    }

    @Test
    public void satisfiesHasConditions_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.HAS, "has:something");
        verifyQualifierContentError(QualifierType.HAS, "has:2011-1-1");
    }

    @Test
    public void satisfiesNoConditions_validInputs() {
        TurboLabel label = new TurboLabel(REPO, "type.bug");
        TurboUser user = new TurboUser(REPO, "bob", "alice");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertTrue(matches("no:label", issue));
        assertTrue(matches("no:milestone", issue));
        assertTrue(matches("no:assignee", issue));

        // test: keyword aliases  
        assertTrue(matches("no:m", issue));
        assertTrue(matches("no:as", issue));

        issue.addLabel(label);
        IModel model = TestUtils.modelWith(issue, label);

        assertFalse(Qualifier.process(model, Parser.parse("no:label"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("no:assignee"), issue));

        issue.setMilestone(milestone);
        model = TestUtils.modelWith(issue, label, milestone);

        assertFalse(Qualifier.process(model, Parser.parse("no:label"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertTrue(Qualifier.process(model, Parser.parse("no:assignee"), issue));

        issue.setAssignee(user);
        model = TestUtils.modelWith(issue, label, milestone, user);

        assertFalse(Qualifier.process(model, Parser.parse("no:label"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertFalse(Qualifier.process(model, Parser.parse("no:assignee"), issue));
    }

    @Test
    public void satisfiesNoConditions_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.NO, "no:something");
    }

    @Test
    public void satisfiesType_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, true);

        assertFalse(matches("type:issue", issue));
        assertTrue(matches("type:pr", issue));

        // test: qualifier alias
        assertFalse(matches("ty:issue", issue));

        // test: keyword aliases
        assertFalse(matches("type:i", issue));
        assertTrue(matches("type:p", issue));

        issue = new TurboIssue(REPO, 1, "", "", null, false);

        assertTrue(matches("type:issue", issue));
        assertFalse(matches("type:pr", issue));
    }

    @Test
    public void satisfiesType_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.TYPE, "type:something");
        verifyQualifierContentError(QualifierType.TYPE, "type:2011-1-1");
    }

    @Test
    public void satisfiesIsConditions_validInputs() {

        TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, true);

        assertFalse(matches("is:issue", issue));
        assertTrue(matches("is:pr", issue));

        assertTrue(matches("is:open", issue));
        assertTrue(matches("is:unmerged", issue));
        assertFalse(matches("is:closed", issue));
        assertFalse(matches("is:merged", issue));

        // test: keyword aliases
        assertFalse(matches("is:i", issue));
        assertTrue(matches("is:p", issue));
        assertTrue(matches("is:o", issue));
        assertTrue(matches("is:um", issue));
        assertFalse(matches("is:c", issue));
        assertFalse(matches("is:mg", issue));

        issue.setOpen(false);

        assertFalse(matches("is:open", issue));
        assertFalse(matches("is:unmerged", issue));
        assertTrue(matches("is:closed", issue));
        assertTrue(matches("is:merged", issue));

        issue = new TurboIssue(REPO, 1, "", "", null, false);

        assertTrue(matches("is:issue", issue));
        assertFalse(matches("is:pr", issue));

        assertTrue(matches("is:open", issue));
        assertFalse(matches("is:closed", issue));

        // Not a PR
        assertFalse(matches("is:unmerged", issue));
        assertFalse(matches("is:merged", issue));

        issue.setOpen(false);

        assertFalse(matches("is:open", issue));
        assertTrue(matches("is:closed", issue));

        // Not a PR
        assertFalse(matches("is:unmerged", issue));
        assertFalse(matches("is:merged", issue));

        // Read status

        assertFalse(issue.isCurrentlyRead());

        assertTrue(matches("is:unread", issue));
        assertTrue(matches("is:ur", issue));
        assertFalse(matches("is:read", issue));
        assertFalse(matches("is:rd", issue));

        issue.setUpdatedAt(LocalDateTime.of(2015, 2, 17, 2, 10));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));

        assertTrue(matches("is:unread", issue));
        assertFalse(matches("is:read", issue));

        issue.setUpdatedAt(LocalDateTime.of(2015, 1, 1, 1, 1));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));

        assertFalse(matches("is:unread", issue));
        assertTrue(matches("is:read", issue));
    }

    @Test
    public void satisfiesIs_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.IS, "is:something");
        verifyQualifierContentError(QualifierType.IS, "is:2011-1-1");
    }

    @Test
    public void satisfiesCreatedDate_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "", LocalDateTime.of(2014, 12, 2, 12, 0), false);

        assertFalse(matches("created:<2014-12-1", issue));
        assertFalse(matches("created:<=2014-12-1", issue));
        assertTrue(matches("created:>2014-12-1", issue));
        assertTrue(matches("created:2014-12-2", issue));

        // test: qualifier alias
        assertFalse(matches("cr:<2014-12-1", issue));
    }

    @Test
    public void satisfiesCreationDate_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.CREATED, "created:nondate");
    }

    @Test
    public void satisfiesUpdatedHours_validInputs() {
        LocalDateTime now = LocalDateTime.now();
        Qualifier.setCurrentTime(now);

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setUpdatedAt(now.minusDays(2));

        assertFalse(matches("updated:<24", issue));
        assertEquals(matches("updated:<24", issue),
                     matches("updated:24", issue));
        assertTrue(matches("updated:>24", issue));

        // test: qualifier alias
        assertFalse(matches("u:<24", issue));

        issue = new TurboIssue(REPO, 1, "");
        issue.setUpdatedAt(now.minusDays(1));

        assertTrue(matches("updated:<26", issue));
        assertEquals(matches("updated:<26", issue),
                     matches("updated:26", issue));
        assertFalse(matches("updated:>26", issue));
    }

    @Test
    public void satisfiesUpdatedHours_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.UPDATED, "updated:nondate");
    }

    @Test
    public void satisfiesRepo_validInputs() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertTrue(matches("repo:" + REPO, issue));
        assertFalse(matches("repo:something/else", issue));

        // test: qualifier alias
        assertTrue(matches("r:" + REPO, issue));
    }

    @Test
    public void satisfiesRepo_invalidInputs_throwSemanticException() {
        verifyQualifierContentError(QualifierType.REPO, "repo:2011-1-1");

        // test: compound id lookup
        verifyQualifierContentError(QualifierType.REPO, "id:2011#1");
    }

    @Test
    public void filterLabelMatching() {
        // Without group
        assertFalse(Qualifier.labelMatches("pri", "priority.high"));
        assertTrue(Qualifier.labelMatches("hi", "p.high"));

        // With group
        assertTrue(Qualifier.labelMatches("ior.hi", "priority.high"));
        assertTrue(Qualifier.labelMatches("ior.ig", "priority.high"));
        assertTrue(Qualifier.labelMatches("pri.hi", "priority.high"));
        assertTrue(Qualifier.labelMatches("p.hi", "p.high"));

        // Case
        assertTrue(Qualifier.labelMatches("p.hi", "P.high"));
        assertTrue(Qualifier.labelMatches("p.hi", "PRIORITY.high"));
        assertTrue(Qualifier.labelMatches("PRIO.hi", "PRIORITY.high"));

        // Disambiguation
        assertTrue(Qualifier.labelMatches("p.", "p.high"));
        assertTrue(Qualifier.labelMatches(".hi", ".high"));

        // Non-matches
        assertFalse(Qualifier.labelMatches("pri.hi", "p.high"));
        assertFalse(Qualifier.labelMatches("pi.hi", "p.high"));
        assertFalse(Qualifier.labelMatches(".", "p.high"));
    }

    @Test
    public void processQualifier_useInvalidUsername_getUsernameWarning() {
        TurboUser user = new TurboUser(REPO, "fox", "charlie");
        IModel model = TestUtils.singletonModel(createModelFromUsers(REPO, user));
        verifyUserWarning(model, "involves:bOb",
                          Arrays.asList(String.format(USER_WARNING_ERROR_FORMAT, "bOb", REPO)));
        verifyUserWarning(model, "involves:foxX",
                          Arrays.asList(String.format(USER_WARNING_ERROR_FORMAT, "foxX", REPO)));
        verifyUserWarning(model, "author:alice",
                          Arrays.asList(String.format(USER_WARNING_ERROR_FORMAT, "alice", REPO)));
    }

    @Test
    public void processQualifier_useValidUsername_noUsernameWarning() {
        TurboUser user = new TurboUser(REPO, "fox", "charlie");
        IModel model = TestUtils.singletonModel(createModelFromUsers(REPO, user));
        verifyUserWarning(model, "involves:fOX", new ArrayList<>());
        verifyUserWarning(model, "assignee:FOX", new ArrayList<>());
        verifyUserWarning(model, "assignee:CHAR", new ArrayList<>());
    }

    @Test
    public void processQualifier() {
        TurboUser user1 = new TurboUser(REPO, "alice", "Alice");
        TurboUser user2 = new TurboUser(REPO, "bob", "Bob");
        TurboUser user3 = new TurboUser(REPO, "fox", "Charlie");
        TurboIssue issue1 = new TurboIssue(REPO, 1, "title", "alice", LocalDateTime.now(), false);
        TurboIssue issue2 = new TurboIssue(REPO, 1, "title", "bob", LocalDateTime.now(), false);
        issue1.setAssignee(user3);
        IModel model = TestUtils.singletonModel(createModelFromUsers(REPO, user1, user2, user3));
        assertTrue(Qualifier.process(model, Parser.parse("assignee:ox"), issue1));
        assertTrue(Qualifier.process(model, Parser.parse("author:alice"), issue1));
        assertFalse(Qualifier.process(model, Parser.parse("assignee:charlie"), issue2));
        assertTrue(Qualifier.process(model, Parser.parse("author:bob"), issue2));
        assertTrue(Qualifier.process(model, Parser.parse("author:bob"), issue2));
    }


    /**
     * Tests the filter string in the context of an empty model
     */
    public boolean matches(String filterExpr, TurboIssue issue) {
        return Qualifier.process(empty, Parser.parse(filterExpr), issue);
    }

    /**
     * Confirms that the input causes a Semantic Exception to be thrown
     * with correct error message. Test fails if it doesn't.
     */
    private void verifySemanticException(IModel model, String input, String warningMessage) {
        TurboIssue issue = new TurboIssue(REPO, 1, "title");
        thrown.expect(SemanticException.class);
        thrown.expectMessage(warningMessage);
        Qualifier.process(model, Parser.parse(input), issue);
    }

    private void verifyQualifierContentError(QualifierType type, String invalidInput) {
        verifySemanticException(empty, invalidInput, String.format(SemanticException.ERROR_MESSAGE,
                                                                   type, type.getDescriptionOfValidInputs()));
    }

    private void verifyUserWarning(IModel model, String input, List<String> expectedWarnings) {
        TurboIssue issue = new TurboIssue(REPO, 1, "title");
        FilterExpression filterExpr = Parser.parse(input);
        List<String> warnings = filterExpr.getWarnings(model, issue);
        assertEquals(expectedWarnings, warnings);
    }

    private Model createModelFromUsers(String name, TurboUser... users) {
        return new Model(name, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), Arrays.asList(users));
    }
}
