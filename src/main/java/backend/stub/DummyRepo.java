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
import ui.UI;
import util.events.UpdateDummyRepoEventHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DummyRepo implements Repo {

	private static final String DUMMY_REPO_ID = "dummy/dummy";

	private static int issueCounter = 0;
	private static int labelCounter = 0;
	private static int milestoneCounter = 0;
	private static int userCounter = 0;

	private static List<TurboIssue> issues = new ArrayList<>();
	private static List<TurboLabel> labels = new ArrayList<>();
	private static List<TurboMilestone> milestones = new ArrayList<>();
	private static List<TurboUser> users = new ArrayList<>();

	public DummyRepo() {
		for (int i=0; i<10; i++) {
			issues.add(makeDummyIssue(DUMMY_REPO_ID));
			labels.add(makeDummyLabel(DUMMY_REPO_ID));
			milestones.add(makeDummyMilestone(DUMMY_REPO_ID));
			users.add(makeDummyUser(DUMMY_REPO_ID));
		}

		UI.events.registerEvent((UpdateDummyRepoEventHandler) e -> {
			switch (e.updateType) {
				case NEW_ISSUE:
					issues.add(makeDummyIssue(DUMMY_REPO_ID));
					break;
			}
		});
	}

	@Override
	public boolean login(UserCredentials credentials) {
		if (credentials.username.equals("test") && credentials.password.equals("test")) return true;
		return false;
	}

	@Override
	public ImmutableTriple<List<TurboIssue>, String, Date>
		getUpdatedIssues(String repoId, String ETag, Date lastCheckTime) {
		updateRandomIssue(DUMMY_REPO_ID);
		issues.add(makeDummyIssue(DUMMY_REPO_ID));
		return new ImmutableTriple<>(issues, ETag, lastCheckTime);
	}

	@Override
	public ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String repoId, String ETag) {
		updateRandomLabel(DUMMY_REPO_ID);
		labels.add(makeDummyLabel(DUMMY_REPO_ID));
		return new ImmutablePair<>(labels, ETag);
	}

	@Override
	public ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String repoId, String ETag) {
		updateRandomMilestone(DUMMY_REPO_ID);
		milestones.add(makeDummyMilestone(DUMMY_REPO_ID));
		return new ImmutablePair<>(milestones, ETag);
	}

	@Override
	public ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String repoId, String ETag) {
		updateRandomUser(DUMMY_REPO_ID);
		users.add(makeDummyUser(DUMMY_REPO_ID));
		return new ImmutablePair<>(users, ETag);
	}

	@Override
	public List<TurboIssue> getIssues(String repoName) {
		return issues;
	}

	@Override
	public List<TurboLabel> getLabels(String repoId) {
		return labels;
	}

	@Override
	public List<TurboMilestone> getMilestones(String repoId) {
		return milestones;
	}

	@Override
	public List<TurboUser> getCollaborators(String repoId) {
		return users;
	}

	private static TurboIssue updateRandomIssue(String repoId) {
		int i = (int) (Math.random() * issueCounter);
		return issues.set(i, new TurboIssue(repoId, i + 1, "Issue " + (i + 1) + " " + Math.random()));
	}

	private static TurboIssue makeDummyIssue(String repoId) {
		TurboIssue issue = new TurboIssue(repoId, issueCounter + 1, "Issue " + (issueCounter + 1));
		issueCounter++;
		return issue;
	}

	private static TurboLabel updateRandomLabel(String repoId) {
		int i = (int) (Math.random() * labelCounter);
		return labels.set(i, new TurboLabel(repoId, "Label " + (i + 1) + " " + Math.random()));
	}

	private static TurboLabel makeDummyLabel(String repoId) {
		TurboLabel label = new TurboLabel(repoId, "Label " + (labelCounter + 1));
		labelCounter++;
		return label;
	}

	private static TurboMilestone updateRandomMilestone(String repoId) {
		int i = (int) (Math.random() * milestoneCounter);
		return milestones.set(i, new TurboMilestone(repoId, (i + 1), "Milestone " + (i + 1) + " " + Math.random()));
	}

	private static TurboMilestone makeDummyMilestone(String repoId) {
		TurboMilestone milestone = new TurboMilestone(repoId, milestoneCounter + 1,
			"Milestone " + (milestoneCounter + 1));
		milestoneCounter++;
		return milestone;
	}

	private static TurboUser updateRandomUser(String repoId) {
		int i = (int) (Math.random() * userCounter);
		return users.set(i, new TurboUser(repoId, "User " + (i + 1) + " " + Math.random()));
	}

	private static TurboUser makeDummyUser(String repoId) {
		TurboUser user = new TurboUser(repoId, "User " + (userCounter + 1));
		userCounter++;
		return user;
	}

	@Override
	public List<TurboIssueEvent> getEvents(String repoId, int issueId) {
		return new ArrayList<>();
	}

	@Override
	public List<Comment> getComments(String repoId, int issueId) {
		return new ArrayList<>();
	}

	@Override
	public boolean isRepositoryValid(String repoId) {
		return true;
	}

}
