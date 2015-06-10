//package tests;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import backend.resource.*;
//import org.eclipse.egit.github.core.Issue;
//import org.eclipse.egit.github.core.Label;
//import org.eclipse.egit.github.core.Milestone;
//import org.eclipse.egit.github.core.User;
//
//public class TestUtils {
//
//	/**
//	 * Methods for generating GitHub stub data.
//	 */
//
//	/**
//	 * Generates a list of Issues of the given length.
//	 *
//	 * @param number
//	 * @return
//	 */
//	public static List<Issue> getStubIssues(int number) {
//		ArrayList<Issue> issues = new ArrayList<>();
//
//		User user = new User();
//		user.setLogin("tester");
//
//		for (int i = 0; i < number; i++) {
//			Issue issue = getStubIssue(i + 1);
//			issues.add(issue);
//		}
//		return issues;
//	}
//
//	public static Issue getStubIssue(int issueId) {
//		User user = new User();
//		user.setLogin("tester");
//
//		Issue issue = new Issue();
//		issue.setTitle("issue" + issueId);
//		issue.setBody("description for issue " + issueId);
//		issue.setNumber(issueId);
//		issue.setState("open");
//		issue.setUser(user);
//		issue.setCreatedAt(new Date());
//		issue.setUpdatedAt(new Date());
//
//		return issue;
//	}
//
//	/**
//	 * Generates a list of Labels of the given length.
//	 *
//	 * @return
//	 */
//	public static List<Label> getStubLabels(int number) {
//		ArrayList<Label> labels = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			Label label = getStubLabel("group.label" + (i + 1));
//			labels.add(label);
//		}
//		return labels;
//	}
//
//	public static Label getStubLabel(String name) {
//
//		Label label = new Label();
//		label.setName(name);
//		return label;
//	}
//
//	/**
//	 * Generates a list of Users of the given length.
//	 *
//	 * @return
//	 */
//	public static List<User> getStubUsers(int number) {
//		ArrayList<User> users = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			User user = getStubUser("user" + (i + 1));
//			users.add(user);
//		}
//		return users;
//	}
//
//	public static User getStubUser(String name) {
//		User milestone = new User();
//		milestone.setLogin(name);
//		return milestone;
//	}
//
//	/**
//	 * Generates a list of Milestones of the given length.
//	 *
//	 * @return
//	 */
//	public static List<Milestone> getStubMilestones(int number) {
//		ArrayList<Milestone> milestones = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			Milestone milestone = getStubMilestone("v0." + (i + 1));
//			milestones.add(milestone);
//		}
//		return milestones;
//	}
//
//	public static Milestone getStubMilestone(String title) {
//		Milestone milestone = new Milestone();
//		milestone.setTitle(title);
//		return milestone;
//	}
//
//	/**
//	 * Methods for generating Turbo* stub data.
//	 */
//
//	/**
//	 * Generates a list of TurboIssues of the given length.
//	 *
//	 * @param model
//	 * @param number
//	 * @return
//	 */
//	public static List<TurboIssue> getStubTurboIssues(Model model, int number) {
//		ArrayList<TurboIssue> issues = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			TurboIssue issue = getStubTurboIssue(model, i + 1);
//			issues.add(issue);
//		}
//		return issues;
//	}
//
//	public static TurboIssue getStubTurboIssue(Model model, int issueId) {
//		TurboIssue issue = new TurboIssue(issueId, "issue" + issueId);
//		issue.setDescription("description for issue " + issueId);
//		return issue;
//	}
//
//	/**
//	 * Generates a list of TurboLabels of the given length.
//	 *
//	 * @return
//	 */
//	public static List<TurboLabel> getStubTurboLabels(int number) {
//		ArrayList<TurboLabel> labels = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			TurboLabel label = getStubTurboLabel("group", "name" + (i + 1));
//			labels.add(label);
//		}
//		return labels;
//	}
//
//	public static TurboLabel getStubTurboLabel(String group, String name) {
//		return new TurboLabel(group, name);
//	}
//
//	/**
//	 * Generates a list of TurboUsers of the given length.
//	 *
//	 * @return
//	 */
//	public static List<TurboUser> getStubTurboUsers(int number) {
//		ArrayList<TurboUser> users = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			TurboUser user = getStubTurboUser("user" + (i + 1));
//			users.add(user);
//		}
//		return users;
//	}
//
//	public static TurboUser getStubTurboUser(String name) {
//		TurboUser user = new TurboUser(name);
//		return user;
//	}
//
//	/**
//	 * Generates a list of TurboMilestones of the given length.
//	 *
//	 * @return
//	 */
//	public static List<TurboMilestone> getStubTurboMilestones(int number) {
//		ArrayList<TurboMilestone> milestones = new ArrayList<>();
//		for (int i = 0; i < number; i++) {
//			TurboMilestone milestone = getStubTurboMilestone("v0." + (i + 1));
//			milestones.add(milestone);
//		}
//		return milestones;
//	}
//
//	public static TurboMilestone getStubTurboMilestone(String name) {
//		return new TurboMilestone(name);
//	}
//}
