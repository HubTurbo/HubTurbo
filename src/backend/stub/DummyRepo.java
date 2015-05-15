package backend.stub;

import backend.UserCredentials;
import backend.interfaces.Repo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DummyRepo implements Repo<Issue, Label, Milestone, User> {

	private static int issueCounter = 10;
	private static int labelCounter = 10;
	private static int milestoneCounter = 10;
	private static int userCounter = 10;

	public DummyRepo() {
	}

	@Override
	public boolean login(UserCredentials credentials) {
		return true;
	}

	@Override
	public ImmutableTriple<List<Issue>, String, Date> getUpdatedIssues(String repoId, String ETag, Date lastCheckTime) {
		List<Issue> issues = new ArrayList<>();
		issues.add(updateRandomIssue());
		issues.add(makeDummyIssue());
		return new ImmutableTriple<>(issues, ETag, lastCheckTime);
	}

	@Override
	public ImmutablePair<List<Label>, String> getUpdatedLabels(String repoId, String ETag) {
		List<Label> labels = new ArrayList<>();
		labels.add(updateRandomLabel());
		labels.add(makeDummyLabel());
		return new ImmutablePair<>(labels, ETag);
	}

	@Override
	public ImmutablePair<List<Milestone>, String> getUpdatedMilestones(String repoId, String ETag) {
		List<Milestone> milestones = new ArrayList<>();
		milestones.add(updateRandomMilestone());
		milestones.add(makeDummyMilestone());
		return new ImmutablePair<>(milestones, ETag);
	}

	@Override
	public ImmutablePair<List<User>, String> getUpdatedUsers(String repoId, String ETag) {
		List<User> users = new ArrayList<>();
		users.add(updateRandomUser());
		users.add(makeDummyUser());
		return new ImmutablePair<>(users, ETag);
	}

	@Override
	public List<Issue> getIssues(String repoName) {
		List<Issue> issues = new ArrayList<>();
		for (int i=0; i<10; i++) {
			issues.add(makeDummyIssue());
		}
		return issues;
	}

	@Override
	public List<Label> getLabels(String repoId) {
		List<Label> labels = new ArrayList<>();
		for (int i=0; i<10; i++) {
			labels.add(makeDummyLabel());
		}
		return labels;
	}

	@Override
	public List<Milestone> getMilestones(String repoId) {
		List<Milestone> milestones = new ArrayList<>();
		for (int i=0; i<10; i++) {
			milestones.add(makeDummyMilestone());
		}
		return milestones;
	}

	@Override
	public List<User> getUsers(String repoId) {
		List<User> users = new ArrayList<>();
		for (int i=0; i<10; i++) {
			users.add(makeDummyUser());
		}
		return users;
	}

	private static Issue updateRandomIssue() {
		int i = (int) (Math.random() * issueCounter);
		Issue issue = new Issue();
		issue.setNumber(i);
		issue.setTitle("Issue " + i + " " + Math.random());
		return issue;
	}

	private static Issue makeDummyIssue() {
		Issue issue = new Issue();
		issue.setNumber(issueCounter + 1);
		issue.setTitle("Issue " + (issueCounter + 1));
		issueCounter++;
		return issue;
	}

	private static Label updateRandomLabel() {
		int i = (int) (Math.random() * issueCounter);
		Label label = new Label();
		label.setName("Label " + i + " " + Math.random());
		return label;
	}

	private static Label makeDummyLabel() {
		Label label = new Label();
		label.setName("Label " + (labelCounter + 1));
		labelCounter++;
		return label;
	}

	private static Milestone updateRandomMilestone() {
		int i = (int) (Math.random() * milestoneCounter);
		Milestone milestone = new Milestone();
		milestone.setNumber(i);
		milestone.setTitle("Milestone " + i + " " + Math.random());
		return milestone;
	}

	private static Milestone makeDummyMilestone() {
		Milestone milestone = new Milestone();
		milestone.setTitle("Milestone " + (milestoneCounter + 1));
		milestoneCounter++;
		return milestone;
	}

	private static User updateRandomUser() {
		int i = (int) (Math.random() * userCounter);
		User user = new User();
		user.setName("User " + i + " " + Math.random());
		return user;
	}

	private static User makeDummyUser() {
		User user = new User();
		user.setLogin("User " + (userCounter + 1));
		userCounter++;
		return user;
	}
}
