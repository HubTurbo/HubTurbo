package tests;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.ParseException;
import filter.Parser;
import filter.expression.Qualifier;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FilterEvalTests {

	private final MultiModel empty;
	private static final String REPO = "test/test";

	public FilterEvalTests() {
		empty = new MultiModel();
		empty.setDefaultRepo(REPO);
	}

	@BeforeClass
	public static void setup() {
	}

	@Test
	public void invalid() {
		TurboIssue issue = new TurboIssue(REPO, 1, "title");
		assertEquals(false, Qualifier.process(empty, Parser.parse("something:a"), issue));
	}

	@Test
	public void id() {
		TurboIssue issue = new TurboIssue(REPO, 1, "title");

		assertEquals(true, Qualifier.process(empty, Parser.parse("id:1"), issue));

		// Non-number
		assertEquals(false, Qualifier.process(empty, Parser.parse("id:a"), issue));
	}

	private void testForPresenceOfKeywords(String prefix, TurboIssue issue) {

		// Exact match
		assertEquals(true, Qualifier.process(empty, Parser.parse(prefix + "test"), issue));

		// Substring
		assertEquals(true, Qualifier.process(empty, Parser.parse(prefix + "te"), issue));

		// Implicit conjunction
		assertEquals(true, Qualifier.process(empty, Parser.parse(prefix + "is a"), issue));

		// Like above but out of order
		assertEquals(true, Qualifier.process(empty, Parser.parse(prefix + "a is"), issue));
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

		assertEquals(false, Qualifier.process(empty, Parser.parse("in:something test"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("in:something te"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("in:something is a"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("in:something a is"), issue));
	}

	private IModel singletonModel(Model model) {
		MultiModel models = new MultiModel();
		models.add(model);
		models.setDefaultRepo(model.getRepoId());
		return models;
	}

	private IModel modelWith(TurboIssue issue, TurboMilestone milestone) {
		return singletonModel(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(),
			new ArrayList<>(Arrays.asList(milestone)),
			new ArrayList<>()));
	}

	private IModel modelWith(TurboIssue issue, TurboLabel label) {
		return singletonModel(new Model(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(Arrays.asList(label)),
			new ArrayList<>(),
			new ArrayList<>())));
	}

	private IModel modelWith(TurboIssue issue, TurboUser user) {
		return singletonModel(new Model(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(),
			new ArrayList<>(),
			new ArrayList<>(Arrays.asList(user)))));
	}

	private IModel modelWith(TurboIssue issue, TurboLabel label, TurboMilestone milestone) {
		return singletonModel(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(Arrays.asList(label)),
			new ArrayList<>(Arrays.asList(milestone)),
			new ArrayList<>()));
	}

	private IModel modelWith(TurboIssue issue, TurboLabel label, TurboMilestone milestone, TurboUser user) {
		return singletonModel(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(Arrays.asList(label)),
			new ArrayList<>(Arrays.asList(milestone)),
			new ArrayList<>(Arrays.asList(user))));
	}

	@Test
	public void milestone() {
		TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

		TurboIssue issue = new TurboIssue(REPO, 1, "");
		issue.setMilestone(milestone);

		IModel model = modelWith(issue, milestone);

		assertEquals(true, Qualifier.process(model, Parser.parse("milestone:v1.0"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("milestone:v1"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("milestone:v"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("milestone:1"), issue));
		try {
			assertEquals(true, Qualifier.process(model, Parser.parse("milestone:."), issue));
			fail(". is not a valid token on its own");
		} catch (ParseException ignored) {
		}
		assertEquals(false, Qualifier.process(empty, Parser.parse("milestone:what"), issue));
	}

	@Test
	public void label() {
		TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");

		TurboIssue issue = new TurboIssue(REPO, 1, "");
		issue.addLabel(label);

		IModel model = modelWith(issue, label);

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

		model = modelWith(issue, label);

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

		IModel model = modelWith(issue, user);

		assertEquals(true, Qualifier.process(model, Parser.parse("assignee:BOB"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("assignee:bob"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("assignee:alice"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("assignee:o"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("assignee:lic"), issue));
	}

	@Test
	public void author() {
		TurboIssue issue = new TurboIssue(REPO, 1, "", "bob", null, false);

		assertEquals(true, Qualifier.process(empty, Parser.parse("creator:BOB"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("creator:bob"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("creator:alice"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("creator:o"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("creator:lic"), issue));

		assertEquals(true, Qualifier.process(empty, Parser.parse("author:BOB"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("author:bob"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("author:alice"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("author:o"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("author:lic"), issue));
	}

	@Test
	public void involves() {
		// involves = assignee || author

		// assignee
		TurboUser user = new TurboUser(REPO, "bob", "alice");

		TurboIssue issue = new TurboIssue(REPO, 1, "");
		issue.setAssignee(user);

		IModel model = modelWith(issue, user);

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
		assertEquals(false, Qualifier.process(empty, Parser.parse("state:open"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("state:o"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("state:closed"), issue));
	}

	@Test
	public void has() {
		TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");
		TurboUser user = new TurboUser(REPO, "bob", "alice");
		TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

		TurboIssue issue = new TurboIssue(REPO, 1, "");

		assertEquals(false, Qualifier.process(empty, Parser.parse("has:label"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("has:milestone"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("has:assignee"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("has:something"), issue));

		issue.addLabel(label);
		IModel model = modelWith(issue, label);

		assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("has:milestone"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("has:assignee"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("has:something"), issue));

		issue.setMilestone(milestone);
		model = modelWith(issue, label, milestone);

		assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("has:milestone"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("has:assignee"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("has:something"), issue));

		issue.setAssignee(user);
		model = modelWith(issue, label, milestone, user);

		assertEquals(true, Qualifier.process(model, Parser.parse("has:label"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("has:milestone"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("has:assignee"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("has:something"), issue));
	}

	@Test
	public void no() {
		TurboLabel label = TurboLabel.exclusive(REPO, "type", "bug");
		TurboUser user = new TurboUser(REPO, "bob", "alice");
		TurboMilestone milestone = new TurboMilestone(REPO, 1, "v1.0");

		TurboIssue issue = new TurboIssue(REPO, 1, "");

		assertEquals(true, Qualifier.process(empty, Parser.parse("no:label"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("no:milestone"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("no:assignee"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("no:something"), issue));

		issue.addLabel(label);
		IModel model = modelWith(issue, label);

		assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("no:milestone"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("no:assignee"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("no:something"), issue));

		issue.setMilestone(milestone);
		model = modelWith(issue, label, milestone);

		assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("no:milestone"), issue));
		assertEquals(true, Qualifier.process(model, Parser.parse("no:assignee"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("no:something"), issue));

		issue.setAssignee(user);
		model = modelWith(issue, label, milestone, user);

		assertEquals(false, Qualifier.process(model, Parser.parse("no:label"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("no:milestone"), issue));
		assertEquals(false, Qualifier.process(model, Parser.parse("no:assignee"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("no:something"), issue));
	}

	@Test
	public void type() {
		TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, true);

		assertEquals(false, Qualifier.process(empty, Parser.parse("type:issue"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("type:pr"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("type:sldkj"), issue));

		issue = new TurboIssue(REPO, 1, "", "", null, false);

		assertEquals(true, Qualifier.process(empty, Parser.parse("type:issue"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("type:pr"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("type:lkjs"), issue));
	}

	@Test
	public void is() {

		TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, true);

		assertEquals(false, Qualifier.process(empty, Parser.parse("is:issue"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("is:pr"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:sldkj"), issue));

		assertEquals(true, Qualifier.process(empty, Parser.parse("is:open"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("is:unmerged"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:closed"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:merged"), issue));

		issue.setOpen(false);

		assertEquals(false, Qualifier.process(empty, Parser.parse("is:open"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:unmerged"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("is:closed"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("is:merged"), issue));

		issue = new TurboIssue(REPO, 1, "", "", null, false);

		assertEquals(true, Qualifier.process(empty, Parser.parse("is:issue"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:pr"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:lkjs"), issue));

		assertEquals(true, Qualifier.process(empty, Parser.parse("is:open"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:closed"), issue));

		// Not a PR
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:unmerged"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:merged"), issue));

		issue.setOpen(false);

		assertEquals(false, Qualifier.process(empty, Parser.parse("is:open"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("is:closed"), issue));

		// Not a PR
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:unmerged"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("is:merged"), issue));
	}

	@Test
	public void created() {
		TurboIssue issue = new TurboIssue(REPO, 1, "", "", LocalDateTime.of(2014, 12, 2, 12, 0), false);

		assertEquals(false, Qualifier.process(empty, Parser.parse("created:<2014-12-1"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("created:<=2014-12-1"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("created:>2014-12-1"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("created:2014-12-2"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("created:nondate"), issue));
	}

	@Test
	public void updated() {
		LocalDateTime now = LocalDateTime.now();
		Qualifier.setCurrentTime(now);

		TurboIssue issue = new TurboIssue(REPO, 1, "");
		issue.setUpdatedAt(now.minusDays(2));

		assertEquals(false, Qualifier.process(empty, Parser.parse("updated:<24"), issue));
		assertEquals(Qualifier.process(empty, Parser.parse("updated:<24"), issue),
			Qualifier.process(empty, Parser.parse("updated:24"), issue));
		assertEquals(true, Qualifier.process(empty, Parser.parse("updated:>24"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("updated:nondate"), issue));

		issue = new TurboIssue(REPO, 1, "");
		issue.setUpdatedAt(now.minusDays(1));

		assertEquals(true, Qualifier.process(empty, Parser.parse("updated:<26"), issue));
		assertEquals(Qualifier.process(empty, Parser.parse("updated:<26"), issue),
			Qualifier.process(empty, Parser.parse("updated:26"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("updated:>26"), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("updated:nondate"), issue));
	}

	@Test
	public void repo() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");

		assertEquals(true, Qualifier.process(empty, Parser.parse("repo:" + REPO), issue));
		assertEquals(false, Qualifier.process(empty, Parser.parse("repo:something/else"), issue));
	}
}
