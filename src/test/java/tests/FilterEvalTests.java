package tests;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.ParseException;
import filter.Parser;
import filter.expression.Qualifier;
import org.junit.BeforeClass;
import org.junit.Test;

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

	private void testForPresenceOfKeywords(TurboIssue issue) {
		testForPresenceOfKeywords("", issue);
	}

	@Test
	public void title() {
		TurboIssue issue = new TurboIssue(REPO, 1, "this is a test");
		testForPresenceOfKeywords(issue);
	}

	@Test
	public void body() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");
		issue.setDescription("this is a test");
		testForPresenceOfKeywords(issue);
	}

	@Test
	public void in() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");
		issue.setDescription("this is a test");
		testForPresenceOfKeywords("in:body ", issue);

		issue = new TurboIssue(REPO, 1, "this is a test");
		testForPresenceOfKeywords("in:title ", issue);
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

		assertEquals(Qualifier.process(model, Parser.parse("assignee:BOB"), issue), true);
		assertEquals(Qualifier.process(model, Parser.parse("assignee:bob"), issue), true);
		assertEquals(Qualifier.process(model, Parser.parse("assignee:alice"), issue), true);
		assertEquals(Qualifier.process(model, Parser.parse("assignee:o"), issue), true);
		assertEquals(Qualifier.process(model, Parser.parse("assignee:lic"), issue), true);
	}

//	@Test
//	public void author() {
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setCreator("bob");
//
//		assertEquals(Qualifier.process(Parser.parse("author:BOB"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("author:bob"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("author:alice"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("author:o"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("author:lic"), issue), false);
//	}
//
//	@Test
//	public void involves() {
//		// involves = assignee || author
//
//		// assignee
//		TurboUser user = createUser("bob", "alice");
//		model.addCollaborator(user);
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setAssignee(user);
//
//		assertEquals(Qualifier.process(Parser.parse("involves:BOB"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:bob"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:alice"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:o"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:lic"), issue), true);
//
//		// author
//		issue = new TurboIssue("", "", model);
//		issue.setCreator("bob");
//
//		assertEquals(Qualifier.process(Parser.parse("involves:BOB"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:bob"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:alice"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("involves:o"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("involves:lic"), issue), false);
//	}
//
//	@Test
//	public void state() {
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setOpen(false);
//		assertEquals(Qualifier.process(Parser.parse("state:open"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("state:o"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("state:closed"), issue), true);
//	}
//
//	@Test
//	public void has() {
//		TurboLabel label = createLabel("type", "bug");
//		TurboUser user = createUser("bob", "alice");
//		TurboMilestone milestone = createMilestone("v1.0");
//
//		TurboIssue issue = new TurboIssue("", "", model);
//
//		assertEquals(Qualifier.process(Parser.parse("has:label"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("has:milestone"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("has:assignee"), issue), false);
//
//		issue.addLabel(label);
//
//		assertEquals(Qualifier.process(Parser.parse("has:label"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("has:milestone"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("has:assignee"), issue), false);
//
//		issue.setMilestone(milestone);
//
//		assertEquals(Qualifier.process(Parser.parse("has:label"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("has:milestone"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("has:assignee"), issue), false);
//
//		issue.setAssignee(user);
//
//		assertEquals(Qualifier.process(Parser.parse("has:label"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("has:milestone"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("has:assignee"), issue), true);
//	}
//
//	@Test
//	public void no() {
//		TurboLabel label = createLabel("type", "bug");
//		TurboUser user = createUser("bob", "alice");
//		TurboMilestone milestone = createMilestone("v1.0");
//
//		TurboIssue issue = new TurboIssue("", "", model);
//
//		assertEquals(Qualifier.process(Parser.parse("no:label"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("no:milestone"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("no:assignee"), issue), true);
//
//		issue.addLabel(label);
//
//		assertEquals(Qualifier.process(Parser.parse("no:label"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("no:milestone"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("no:assignee"), issue), true);
//
//		issue.setMilestone(milestone);
//
//		assertEquals(Qualifier.process(Parser.parse("no:label"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("no:milestone"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("no:assignee"), issue), true);
//
//		issue.setAssignee(user);
//
//		assertEquals(Qualifier.process(Parser.parse("no:label"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("no:milestone"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("no:assignee"), issue), false);
//	}
//
//	@Test
//	public void type() {
//		PullRequest pr = new PullRequest();
//		pr.setUrl("something");
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setPullRequest(pr);
//
//		assertEquals(Qualifier.process(Parser.parse("is:issue"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("is:pr"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("is:ssu"), issue), false);
//
//		issue = new TurboIssue("", "", model);
//
//		assertEquals(Qualifier.process(Parser.parse("is:issue"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("is:pr"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("is:ssu"), issue), false);
//	}
//
//	@Test
//	public void is() {
//		PullRequest pr = new PullRequest();
//		pr.setUrl("something");
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setPullRequest(pr);
//		issue.setOpen(false);
//
//		assertEquals(Qualifier.process(Parser.parse("is:merged"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("is:unmerged"), issue), false);
//
//		issue.setOpen(true);
//
//		assertEquals(Qualifier.process(Parser.parse("is:merged"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("is:unmerged"), issue), true);
//
//		// The rest are delegated to state and type, so this should pass if they pass
//	}
//
//	@Test
//	public void created() {
//		TurboIssue issue = new TurboIssue("", "", model);
//		Date date = new Date(Utility.localDateTimeToLong(LocalDateTime.of(2014, 12, 2, 12, 0)));
//		issue.setCreatedAt(new SimpleDateFormat("d MMM yy, h:mm a").format(date));
//
//		assertEquals(Qualifier.process(Parser.parse("created:<2014-12-1"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("created:<=2014-12-1"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("created:>2014-12-1"), issue), true);
//	}
//
//	@Test
//	public void updated() {
//		LocalDateTime now = LocalDateTime.now();
//		Qualifier.setCurrentTime(now);
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setUpdatedAt(now.minusDays(2));
//
//		assertEquals(Qualifier.process(Parser.parse("updated:<24"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("updated:>24"), issue), true);
//
//		issue = new TurboIssue("", "", model);
//		issue.setUpdatedAt(now.minusDays(1));
//
//		assertEquals(Qualifier.process(Parser.parse("updated:<26"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("updated:>26"), issue), false);
//	}

}
