//package tests;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.util.Date;
//
//import backend.resource.*;
//import org.eclipse.egit.github.core.PullRequest;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import util.Utility;
//import filter.ParseException;
//import filter.Parser;
//import filter.expression.Qualifier;
//
//public class FilterEvalTests {
//
//	private final Model model = new Model();
//
//	@BeforeClass
//	public static void setup() {
//		Model.isInTestMode = true;
//	}
//
//	/**
//	 * Tests for the presence of keywords in a particular issue.
//	 *
//	 * @param issue
//	 */
//	private void testForKeywords(String prefix, TurboIssue issue) {
//		assertEquals(Qualifier.process(Parser.parse("test"), issue), true);
//
//		// Substring
//		assertEquals(Qualifier.process(Parser.parse("te"), issue), true);
//
//		// Implicit conjunction
//		assertEquals(Qualifier.process(Parser.parse("is a"), issue), true);
//
//		// Like above but out of order
//		assertEquals(Qualifier.process(Parser.parse("a is"), issue), true);
//	}
//
//	private void testForKeywords(TurboIssue issue) {
//		testForKeywords("", issue);
//	}
//
//	/**
//	 * Creates a milestone and takes care of adding it to the model.
//	 *
//	 * @param title
//	 * @return
//	 */
//	private TurboMilestone createMilestone(String title) {
//		TurboMilestone milestone = new TurboMilestone(title);
//		model.addMilestone(milestone);
//		return milestone;
//	}
//
//	/**
//	 * Creates a label and takes care of adding it to the model.
//	 *
//	 * @param group
//	 * @param name
//	 * @return
//	 */
//	private TurboLabel createLabel(String group, String name) {
//		TurboLabel label = new TurboLabel();
//		label.setGroup(group);
//		label.setName(name);
//		model.addLabel(label);
//		return label;
//	}
//
//	/**
//	 * Creates a user and takes care of adding it to the model.
//	 *
//	 * @param realName
//	 * @param gitHubName
//	 * @return
//	 */
//	private TurboUser createUser(String gitHubName, String realName) {
//		TurboUser user = new TurboUser();
//		user.setGithubName(gitHubName);
//		user.setRealName(realName);
//		model.addCollaborator(user);
//		return user;
//	}
//
//	@Test
//	public void id() {
//		TurboIssue issue = new TurboIssue("1", "desc", model);
//		issue.setId(1);
//
//		assertEquals(Qualifier.process(Parser.parse("id:1"), issue), true);
//
//		// Non-number
//		assertEquals(Qualifier.process(Parser.parse("id:a"), issue), false);
//	}
//
//	@Test
//	public void keyword() {
//		TurboIssue issue = new TurboIssue("", "this is a test", model);
//		testForKeywords(issue);
//	}
//
//	@Test
//	public void title() {
//		TurboIssue issue = new TurboIssue("this is a test", "", model);
//		testForKeywords(issue);
//	}
//
//	@Test
//	public void body() {
//		TurboIssue issue = new TurboIssue("", "this is a test", model);
//		testForKeywords(issue);
//	}
//
//	@Test
//	public void in() {
//		TurboIssue issue = new TurboIssue("", "this is a test", model);
//		testForKeywords("in:body ", issue);
//
//		issue = new TurboIssue("this is a test", "", model);
//		testForKeywords("in:title ", issue);
//	}
//
//	@Test
//	public void milestone() {
//		TurboMilestone milestone = createMilestone("v1.0");
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setMilestone(milestone);
//
//		assertEquals(Qualifier.process(Parser.parse("milestone:v1.0"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("milestone:v1"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("milestone:1"), issue), false);
//		try {
//			assertEquals(Qualifier.process(Parser.parse("milestone:."), issue), true);
//			fail(". is not a valid token on its own");
//		} catch (ParseException e) {
//		}
//		assertEquals(Qualifier.process(Parser.parse("milestone:what"), issue), false);
//	}
//
//	@Test
//	public void parent() {
//		// TODO implement when parent issue feature returns
//	}
//
//	@Test
//	public void label() {
//		TurboLabel label = createLabel("type", "bug");
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.addLabel(label);
//
//		assertEquals(Qualifier.process(Parser.parse("label:type"), issue), false);
//		assertEquals(Qualifier.process(Parser.parse("label:type."), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("label:type.bug"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("label:bug"), issue), true);
//	}
//
//	@Test
//	public void assignee() {
//		TurboUser user = createUser("bob", "alice");
//		model.addCollaborator(user);
//
//		TurboIssue issue = new TurboIssue("", "", model);
//		issue.setAssignee(user);
//
//		assertEquals(Qualifier.process(Parser.parse("assignee:BOB"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("assignee:bob"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("assignee:alice"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("assignee:o"), issue), true);
//		assertEquals(Qualifier.process(Parser.parse("assignee:lic"), issue), true);
//	}
//
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
//
//}
