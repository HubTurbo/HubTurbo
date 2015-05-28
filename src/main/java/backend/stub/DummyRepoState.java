package backend.stub;

import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.TurboIssueEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DummyRepoState {

	private static String dummyRepoId;

	private static int issueCounter = 0;
	private static int labelCounter = 0;
	private static int milestoneCounter = 0;
	private static int userCounter = 0;

	private static List<TurboIssue> issues = new ArrayList<>();
	private static List<TurboLabel> labels = new ArrayList<>();
	private static List<TurboMilestone> milestones = new ArrayList<>();
	private static List<TurboUser> users = new ArrayList<>();

	public DummyRepoState(String repoId) {
		this.dummyRepoId = repoId;
		for (int i=0; i<10; i++) {
			issues.add(makeDummyIssue());
			labels.add(makeDummyLabel());
			milestones.add(makeDummyMilestone());
			users.add(makeDummyUser());
		}
	}

	protected ImmutableTriple<List<TurboIssue>, String, Date>
	getUpdatedIssues(String ETag, Date lastCheckTime) {
		updateRandomIssue();
		issues.add(makeDummyIssue());
		return new ImmutableTriple<>(issues, ETag, lastCheckTime);
	}

	protected ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String ETag) {
		updateRandomLabel();
		labels.add(makeDummyLabel());
		return new ImmutablePair<>(labels, ETag);
	}

	protected ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String ETag) {
		updateRandomMilestone();
		milestones.add(makeDummyMilestone());
		return new ImmutablePair<>(milestones, ETag);
	}

	protected ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String ETag) {
		updateRandomUser();
		users.add(makeDummyUser());
		return new ImmutablePair<>(users, ETag);
	}

	protected List<TurboIssue> getIssues() {
		return issues;
	}

	protected List<TurboLabel> getLabels() {
		return labels;
	}

	protected List<TurboMilestone> getMilestones() {
		return milestones;
	}

	protected List<TurboUser> getCollaborators() {
		return users;
	}

	protected static TurboIssue updateRandomIssue() {
		int i = (int) (Math.random() * issueCounter);
		return issues.set(i, new TurboIssue(dummyRepoId, i + 1, "Issue " + (i + 1) + " " + Math.random()));
	}

	protected static TurboIssue makeDummyIssue() {
		TurboIssue issue = new TurboIssue(dummyRepoId, issueCounter + 1, "Issue " + (issueCounter + 1));
		issueCounter++;
		return issue;
	}

	protected static TurboLabel updateRandomLabel() {
		int i = (int) (Math.random() * labelCounter);
		return labels.set(i, new TurboLabel(dummyRepoId, "Label " + (i + 1) + " " + Math.random()));
	}

	protected static TurboLabel makeDummyLabel() {
		TurboLabel label = new TurboLabel(dummyRepoId, "Label " + (labelCounter + 1));
		labelCounter++;
		return label;
	}

	protected static TurboMilestone updateRandomMilestone() {
		int i = (int) (Math.random() * milestoneCounter);
		return milestones.set(i, new TurboMilestone(dummyRepoId, (i + 1), "Milestone " + (i + 1) + " " + Math.random()));
	}

	protected static TurboMilestone makeDummyMilestone() {
		TurboMilestone milestone = new TurboMilestone(dummyRepoId, milestoneCounter + 1,
			"Milestone " + (milestoneCounter + 1));
		milestoneCounter++;
		return milestone;
	}

	protected static TurboUser updateRandomUser() {
		int i = (int) (Math.random() * userCounter);
		return users.set(i, new TurboUser(dummyRepoId, "User " + (i + 1) + " " + Math.random()));
	}

	protected static TurboUser makeDummyUser() {
		TurboUser user = new TurboUser(dummyRepoId, "User " + (userCounter + 1));
		userCounter++;
		return user;
	}

	protected List<TurboIssueEvent> getEvents() {
		return new ArrayList<>();
	}

	protected List<Comment> getComments() {
		return new ArrayList<>();
	}
}
