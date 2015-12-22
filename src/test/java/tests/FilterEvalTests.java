package tests;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.ParseException;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import org.junit.BeforeClass;
import org.junit.Test;
import prefs.Preferences;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FilterEvalTests {

    private final IModel empty;
    public static final String REPO = "test/test";

    public FilterEvalTests() {
        empty = new MultiModel(mock(Preferences.class));
        empty.setDefaultRepo(REPO);
    }

    @BeforeClass
    public static void setup() {
    }

    /**
     * Helper method for testing an issue against a filter string in the context
     * of an empty model.
     */
    public boolean matches(String filterExpr, TurboIssue issue) {
        return Qualifier.process(empty, Parser.parse(filterExpr), issue);
    }

    @Test
    public void invalid() {
        TurboIssue issue = new TurboIssue(REPO, 1, "title");
        assertEquals(false, matches("something:a", issue));
    }

    @Test
    public void falseQualifier() {
        TurboIssue issue = new TurboIssue(REPO, 1, "1");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        IModel model = TestUtils.modelWith(issue, milestone);

        assertEquals(false, Qualifier.process(model, Qualifier.FALSE, issue));
    }

    @Test
    public void id() {
        TurboIssue issue1 = new TurboIssue(REPO, 1, "1");

        assertEquals(true, matches("id:1", issue1));
        assertEquals(true, matches("id:>=1", issue1));
        assertEquals(true, matches("id:<=1", issue1));
        assertEquals(false, matches("id:<1", issue1));
        assertEquals(false, matches("id:>1", issue1));
        assertEquals(false, matches("id:2", issue1));

        assertEquals(true, matches("id:<2", issue1));
        assertEquals(true, matches("id:<=2", issue1));
        assertEquals(true, matches("id:>0", issue1));
        assertEquals(true, matches("id:>=0", issue1));

        // Non-number
        assertEquals(false, matches("id:a", issue1));
    }

    private void testForPresenceOfKeywords(String prefix, TurboIssue issue) {

        // Exact match
        assertEquals(true, matches(prefix + "test", issue));

        // Substring
        assertEquals(true, matches(prefix + "te", issue));

        // Implicit conjunction
        assertEquals(true, matches(prefix + "is a", issue));

        // Like above but out of order
        assertEquals(true, matches(prefix + "a is", issue));
    }

    @Test
    public void title() {
        TurboIssue issue = new TurboIssue(REPO, 1, "this is a test");
        testForPresenceOfKeywords("title:", issue);
    }

    @Test
    public void body() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setDescription("this is a test");
        testForPresenceOfKeywords("body:", issue);
        testForPresenceOfKeywords("desc:", issue);
        testForPresenceOfKeywords("description:", issue);
    }

    @Test
    public void in() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setDescription("this is a test");
        testForPresenceOfKeywords("in:body ", issue);

        issue = new TurboIssue(REPO, 1, "this is a test");
        testForPresenceOfKeywords("in:title ", issue);

        assertEquals(false, matches("in:something test", issue));
        assertEquals(false, matches("in:something te", issue));
        assertEquals(false, matches("in:something is a", issue));
        assertEquals(false, matches("in:something a is", issue));
    }

    private void testMilestoneParsing(String milestoneQualifier, TurboIssue issue, IModel model) {
        assertEquals(true, Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "v1.0"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "v1"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "v"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse(milestoneQualifier + ":" + "1"), issue));

        try {
            assertEquals(true, Qualifier.process(model, Parser.parse(milestoneQualifier + ":."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }
        assertEquals(false, matches(milestoneQualifier + ":what", issue));
    }

    @Test
    public void milestone() {
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setMilestone(milestone);

        IModel model = TestUtils.modelWith(issue, milestone);

        testMilestoneParsing("milestone", issue, model);
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
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-2"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-1"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+1"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+2"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+3"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        // test: negation alias
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("-milestone:curr"));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(true, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        // test: no milestone in model should return qualifier false
        model = TestUtils.singletonModel(new Model(REPO,
                new ArrayList<>(Arrays.asList(iCurrMin3, iCurrMin2, iCurrMin1,
                        iCurr, iCurrPlus1, iCurrPlus2, iCurrPlus3)),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-3"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-2"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-1"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+1"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+2"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+3"));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin3));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrMin1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurr));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus1));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus2));
        assertEquals(false, Qualifier.process(model, noMilestoneAlias, iCurrPlus3));
    }

    @Test
    public void label() {
        TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.addLabel(label);

        IModel model = TestUtils.modelWith(issue, label);

        assertEquals(false, Qualifier.process(model, Parser.parse("label:type"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("label:type."), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("label:type.bug"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("label:bug"), issue));
        try {
            assertEquals(true, Qualifier.process(model, Parser.parse("label:.bug"), issue));
            fail(". cannot begin symbols");
        } catch (ParseException ignored) {
        }
        try {
            assertEquals(false, Qualifier.process(model, Parser.parse("label:."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }

        // Ensures that an issue isn't rejected by a qualifier just because it has some label
        // that the qualifier doesn't express, i.e. it should only be rejected if it does not
        // have any label that the qualifier expresses. See Qualifier#labelsSatisfy for details.

        label = TurboLabel.exclusive(REPO, "type", "bug");
        TurboLabel label2 = new TurboLabel(REPO, "something");
        issue = new TurboIssue(REPO, 1, "");
        issue.addLabel(label2);
        issue.addLabel(label);

        model = TestUtils.singletonModel(new Model(new Model(REPO,
            new ArrayList<>(Arrays.asList(issue)),
            new ArrayList<>(Arrays.asList(label, label2)),
            new ArrayList<>(),
            new ArrayList<>())));

        assertEquals(true, Qualifier.process(model, Parser.parse("label:t."), issue));

        // Label without a group

        label = new TurboLabel(REPO, "bug");

        issue = new TurboIssue(REPO, 1, "");
        issue.addLabel(label);

        model = TestUtils.modelWith(issue, label);

        assertEquals(false, Qualifier.process(model, Parser.parse("label:bug."), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("label:type.bug"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("label:type"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("label:bug"), issue));
        try {
            assertEquals(true, Qualifier.process(model, Parser.parse("label:.bug"), issue));
            fail(". cannot begin symbols");
        } catch (ParseException ignored) {
        }
        try {
            assertEquals(false, Qualifier.process(model, Parser.parse("label:."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }
    }

    private void testAssigneeParsing(String assigneeQualifier){
        TurboUser user = new TurboUser(REPO, "bob", "alice");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setAssignee(user);

        IModel model = TestUtils.modelWith(issue, user);

        assertEquals(true, Qualifier.process(model, Parser.parse(assigneeQualifier + ":BOB"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse(assigneeQualifier + ":bob"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse(assigneeQualifier + ":alice"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse(assigneeQualifier + ":o"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse(assigneeQualifier + ":lic"), issue));
    }

    @Test
    public void assigneeParsing() {
        testAssigneeParsing("assignee");
    }

    @Test
    public void assigneeWithQualifierAliasParsing() {
        testAssigneeParsing("as");
    }

    @Test
    public void author() {
        testAuthorParsing("author");
    }

    @Test
    public void creator() {
        testAuthorParsing("creator");
    }

    @Test
    public void authorWithQualifierAlias() {
        testAuthorParsing("au");
    }

    private void testAuthorParsing(String authorQualifier){
        TurboIssue issue = new TurboIssue(REPO, 1, "", "bob", null, false);

        assertEquals(true, matches(authorQualifier + ":BOB", issue));
        assertEquals(true, matches(authorQualifier + ":bob", issue));
        assertEquals(false, matches(authorQualifier + ":alice", issue));
        assertEquals(true, matches(authorQualifier + ":o", issue));
        assertEquals(false, matches(authorQualifier + ":lic", issue));
    }

    @Test
    public void involves() {
        // involves = assignee || author

        // assignee
        TurboUser user = new TurboUser(REPO, "bob", "alice");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setAssignee(user);

        IModel model = TestUtils.modelWith(issue, user);

        assertEquals(true, Qualifier.process(model, Parser.parse("involves:BOB"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("involves:bob"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("involves:alice"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("involves:o"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("involves:lic"), issue));

        // author
        issue = new TurboIssue(REPO, 1, "", "bob", null, false);

        assertEquals(true, Qualifier.process(model, Parser.parse("involves:BOB"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("involves:bob"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("involves:alice"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("involves:o"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("involves:lic"), issue));
    }

    @Test
    public void state() {
        testStateParsing("state");
    }

    @Test
    public void stateWithQualifierAlias() {
        testStateParsing("s");
    }

    private void testStateParsing(String stateQualifier){
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setOpen(false);
        assertEquals(false, matches(stateQualifier + ":open", issue));
        assertEquals(false, matches(stateQualifier + ":o", issue));
        assertEquals(true, matches(stateQualifier + ":closed", issue));
    }

    @Test
    public void has() {
        TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");
        TurboUser user = new TurboUser(REPO, "bob", "alice");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertEquals(false, matches("has:label", issue));
        assertEquals(false, matches("has:milestone", issue));
        assertEquals(false, matches("has:m", issue));
        assertEquals(false, matches("has:assignee", issue));
        assertEquals(false, matches("has:as", issue));
        assertEquals(false, matches("has:something", issue));

        issue.addLabel(label);
        IModel model = TestUtils.modelWith(issue, label);

        assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:m"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:assignee"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:as"), issue));
        assertEquals(false, matches("has:something", issue));

        issue.setMilestone(milestone);
        model = TestUtils.modelWith(issue, label, milestone);

        assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:m"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:assignee"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:as"), issue));
        assertEquals(false, matches("has:something", issue));

        issue.setAssignee(user);
        model = TestUtils.modelWith(issue, label, milestone, user);

        assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:m"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:assignee"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:as"), issue));
        assertEquals(false, matches("has:something", issue));
    }

    @Test
    public void no() {
        TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");
        TurboUser user = new TurboUser(REPO, "bob", "alice");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertEquals(true, matches("no:label", issue));
        assertEquals(true, matches("no:milestone", issue));
        assertEquals(true, matches("no:m", issue));
        assertEquals(true, matches("no:assignee", issue));
        assertEquals(true, matches("no:as", issue));
        assertEquals(true, matches("no:something", issue));

        issue.addLabel(label);
        IModel model = TestUtils.modelWith(issue, label);

        assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:m"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:assignee"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:as"), issue));
        assertEquals(true, matches("no:something", issue));

        issue.setMilestone(milestone);
        model = TestUtils.modelWith(issue, label, milestone);

        assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:m"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:assignee"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:as"), issue));
        assertEquals(true, matches("no:something", issue));

        issue.setAssignee(user);
        model = TestUtils.modelWith(issue, label, milestone, user);

        assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:m"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:assignee"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:as"), issue));
        assertEquals(true, matches("no:something", issue));
    }

    @Test
    public void type() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, true);

        assertEquals(false, matches("type:issue", issue));
        assertEquals(true, matches("type:pr", issue));
        assertEquals(false, matches("type:sldkj", issue));

        issue = new TurboIssue(REPO, 1, "", "", null, false);

        assertEquals(true, matches("type:issue", issue));
        assertEquals(false, matches("type:pr", issue));
        assertEquals(false, matches("type:lkjs", issue));
    }

    @Test
    public void is() {

        TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, true);

        assertEquals(false, matches("is:sldkj", issue));

        assertEquals(false, matches("is:issue", issue));
        assertEquals(true, matches("is:pr", issue));

        assertEquals(true, matches("is:open", issue));
        assertEquals(true, matches("is:unmerged", issue));
        assertEquals(false, matches("is:closed", issue));
        assertEquals(false, matches("is:merged", issue));

        issue.setOpen(false);

        assertEquals(false, matches("is:open", issue));
        assertEquals(false, matches("is:unmerged", issue));
        assertEquals(true, matches("is:closed", issue));
        assertEquals(true, matches("is:merged", issue));

        issue = new TurboIssue(REPO, 1, "", "", null, false);

        assertEquals(true, matches("is:issue", issue));
        assertEquals(false, matches("is:pr", issue));

        assertEquals(true, matches("is:open", issue));
        assertEquals(false, matches("is:closed", issue));

        // Not a PR
        assertEquals(false, matches("is:unmerged", issue));
        assertEquals(false, matches("is:merged", issue));

        issue.setOpen(false);

        assertEquals(false, matches("is:open", issue));
        assertEquals(true, matches("is:closed", issue));

        // Not a PR
        assertEquals(false, matches("is:unmerged", issue));
        assertEquals(false, matches("is:merged", issue));

        // Read status

        assertEquals(false, issue.isCurrentlyRead());

        assertEquals(true, matches("is:unread", issue));
        assertEquals(false, matches("is:read", issue));

        issue.setUpdatedAt(LocalDateTime.of(2015, 2, 17, 2, 10));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));

        assertEquals(true, matches("is:unread", issue));
        assertEquals(false, matches("is:read", issue));

        issue.setUpdatedAt(LocalDateTime.of(2015, 1, 1, 1, 1));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));

        assertEquals(false, matches("is:unread", issue));
        assertEquals(true, matches("is:read", issue));
    }

    @Test
    public void created() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "", LocalDateTime.of(2014, 12, 2, 12, 0), false);

        assertEquals(false, matches("created:<2014-12-1", issue));
        assertEquals(false, matches("created:<=2014-12-1", issue));
        assertEquals(true, matches("created:>2014-12-1", issue));
        assertEquals(true, matches("created:2014-12-2", issue));
        assertEquals(false, matches("created:nondate", issue));
    }

    @Test
    public void updated() {
        LocalDateTime now = LocalDateTime.now();
        Qualifier.setCurrentTime(now);

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setUpdatedAt(now.minusDays(2));

        assertEquals(false, matches("updated:<24", issue));
        assertEquals(matches("updated:<24", issue),
            matches("updated:24", issue));
        assertEquals(true, matches("updated:>24", issue));
        assertEquals(false, matches("updated:nondate", issue));

        issue = new TurboIssue(REPO, 1, "");
        issue.setUpdatedAt(now.minusDays(1));

        assertEquals(true, matches("updated:<26", issue));
        assertEquals(matches("updated:<26", issue),
            matches("updated:26", issue));
        assertEquals(false, matches("updated:>26", issue));
        assertEquals(false, matches("updated:nondate", issue));
    }

    @Test
    public void repo() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertEquals(true, matches("repo:" + REPO, issue));
        assertEquals(false, matches("repo:something/else", issue));
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
}
