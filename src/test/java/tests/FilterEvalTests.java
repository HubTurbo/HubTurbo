package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import prefs.Preferences;
import backend.interfaces.IModel;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import filter.ParseException;
import filter.Parser;
import filter.expression.Qualifier;

public class FilterEvalTests {

    private final IModel empty;
    public static final String REPO = "test/test";

    public FilterEvalTests() {
        empty = new MultiModel(new Preferences(true));
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

    @Test
    public void milestone() {
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setMilestone(milestone);

        IModel model = TestUtils.modelWith(issue, milestone);

        assertEquals(true, Qualifier.process(model, Parser.parse("milestone:v1.0"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("milestone:v1"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("milestone:v"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("milestone:1"), issue));
        try {
            assertEquals(true, Qualifier.process(model, Parser.parse("milestone:."), issue));
            fail(". is not a valid token on its own");
        } catch (ParseException ignored) {
        }
        assertEquals(false, matches("milestone:what", issue));
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

    @Test
    public void assignee() {
        TurboUser user = new TurboUser(REPO, "bob", "alice");

        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setAssignee(user);

        IModel model = TestUtils.modelWith(issue, user);

        assertEquals(true, Qualifier.process(model, Parser.parse("assignee:BOB"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("assignee:bob"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("assignee:alice"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("assignee:o"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("assignee:lic"), issue));
    }

    @Test
    public void author() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "bob", null, false);

        assertEquals(true, matches("creator:BOB", issue));
        assertEquals(true, matches("creator:bob", issue));
        assertEquals(false, matches("creator:alice", issue));
        assertEquals(true, matches("creator:o", issue));
        assertEquals(false, matches("creator:lic", issue));

        assertEquals(true, matches("author:BOB", issue));
        assertEquals(true, matches("author:bob", issue));
        assertEquals(false, matches("author:alice", issue));
        assertEquals(true, matches("author:o", issue));
        assertEquals(false, matches("author:lic", issue));
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
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        issue.setOpen(false);
        assertEquals(false, matches("state:open", issue));
        assertEquals(false, matches("state:o", issue));
        assertEquals(true, matches("state:closed", issue));
    }

    @Test
    public void has() {
        TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");
        TurboUser user = new TurboUser(REPO, "bob", "alice");
        TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

        TurboIssue issue = new TurboIssue(REPO, 1, "");

        assertEquals(false, matches("has:label", issue));
        assertEquals(false, matches("has:milestone", issue));
        assertEquals(false, matches("has:assignee", issue));
        assertEquals(false, matches("has:something", issue));

        issue.addLabel(label);
        IModel model = TestUtils.modelWith(issue, label);

        assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:assignee"), issue));
        assertEquals(false, matches("has:something", issue));

        issue.setMilestone(milestone);
        model = TestUtils.modelWith(issue, label, milestone);

        assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("has:assignee"), issue));
        assertEquals(false, matches("has:something", issue));

        issue.setAssignee(user);
        model = TestUtils.modelWith(issue, label, milestone, user);

        assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:milestone"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("has:assignee"), issue));
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
        assertEquals(true, matches("no:assignee", issue));
        assertEquals(true, matches("no:something", issue));

        issue.addLabel(label);
        IModel model = TestUtils.modelWith(issue, label);

        assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:assignee"), issue));
        assertEquals(true, matches("no:something", issue));

        issue.setMilestone(milestone);
        model = TestUtils.modelWith(issue, label, milestone);

        assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertEquals(true, Qualifier.process(model, Parser.parse("no:assignee"), issue));
        assertEquals(true, matches("no:something", issue));

        issue.setAssignee(user);
        model = TestUtils.modelWith(issue, label, milestone, user);

        assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:milestone"), issue));
        assertEquals(false, Qualifier.process(model, Parser.parse("no:assignee"), issue));
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

        issue.setMarkedReadAt(Optional.of(LocalDateTime.now()));

        assertEquals(true, matches("is:unread", issue));
        assertEquals(false, matches("is:read", issue));

        issue.setIsCurrentlyRead(true);

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
}
