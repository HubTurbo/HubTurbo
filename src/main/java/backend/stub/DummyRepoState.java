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

	private String dummyRepoId;

	private List<TurboIssue> issues = new ArrayList<>();
	private List<TurboLabel> labels = new ArrayList<>();
	private List<TurboMilestone> milestones = new ArrayList<>();
	private List<TurboUser> users = new ArrayList<>();

	private List<TurboIssue> updatedIssues = new ArrayList<>();
	private List<TurboLabel> updatedLabels = new ArrayList<>();
	private List<TurboMilestone> updatedMilestones = new ArrayList<>();
	private List<TurboUser> updatedUsers = new ArrayList<>();

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
		ImmutableTriple<List<TurboIssue>, String, Date> toReturn
				= new ImmutableTriple<>(updatedIssues, ETag, lastCheckTime);
		updatedIssues = new ArrayList<>();
		return toReturn;
	}

	protected void makeNewIssue() {
		TurboIssue toAdd = makeDummyIssue();
		issues.add(toAdd);
		updatedIssues.add(toAdd);
	}

	protected ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String ETag) {
		ImmutablePair<List<TurboLabel>, String> toReturn
			= new ImmutablePair<>(updatedLabels, ETag);
		updatedLabels = new ArrayList<>();
		return toReturn;
	}

	protected ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String ETag) {
		ImmutablePair<List<TurboMilestone>, String> toReturn
			= new ImmutablePair<>(updatedMilestones, ETag);
		updatedMilestones = new ArrayList<>();
		return toReturn;
	}

	protected ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String ETag) {
		ImmutablePair<List<TurboUser>, String> toReturn
			= new ImmutablePair<>(updatedUsers, ETag);
		updatedUsers = new ArrayList<>();
		return toReturn;
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

	protected TurboIssue makeDummyIssue() {
		return new TurboIssue(dummyRepoId, issues.size() + 1, "Issue " + (issues.size() + 1));
	}

	protected TurboLabel makeDummyLabel() {
		return new TurboLabel(dummyRepoId, "Label " + (labels.size() + 1));
	}

	protected TurboMilestone makeDummyMilestone() {
		return new TurboMilestone(dummyRepoId, milestones.size() + 1, "Milestone " + (milestones.size() + 1));
	}

	protected TurboUser makeDummyUser() {
		return new TurboUser(dummyRepoId, "User " + (users.size() + 1));
	}

	protected List<TurboIssueEvent> getEvents() {
		return new ArrayList<>();
	}

	protected List<Comment> getComments() {
		return new ArrayList<>();
	}
}
