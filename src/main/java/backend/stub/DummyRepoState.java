package backend.stub;

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
import java.util.UUID;

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
		for (int i = 0; i < 10; i++) {
			makeNewIssue();
			makeNewLabel();
			makeNewMilestone();
			makeNewUser();
		}
	}

	protected ImmutableTriple<List<TurboIssue>, String, Date>
	getUpdatedIssues(String ETag, Date lastCheckTime) {
		String currETag = ETag;
		if (!updatedIssues.isEmpty()) currETag = UUID.randomUUID().toString();

		ImmutableTriple<List<TurboIssue>, String, Date> toReturn
				= new ImmutableTriple<>(updatedIssues, currETag, lastCheckTime);

		updatedIssues = new ArrayList<>();
		return toReturn;
	}

	protected ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String ETag) {
		String currETag = ETag;
		if (!updatedLabels.isEmpty()) currETag = UUID.randomUUID().toString();

		ImmutablePair<List<TurboLabel>, String> toReturn
			= new ImmutablePair<>(updatedLabels, currETag);

		updatedLabels = new ArrayList<>();
		return toReturn;
	}

	protected ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String ETag) {
		String currETag = ETag;
		if (!updatedMilestones.isEmpty()) currETag = UUID.randomUUID().toString();

		ImmutablePair<List<TurboMilestone>, String> toReturn
			= new ImmutablePair<>(updatedMilestones, currETag);

		updatedMilestones = new ArrayList<>();
		return toReturn;
	}

	protected ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String ETag) {
		String currETag = ETag;
		if (!updatedUsers.isEmpty()) currETag = UUID.randomUUID().toString();

		ImmutablePair<List<TurboUser>, String> toReturn
			= new ImmutablePair<>(updatedUsers, currETag);

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

	private TurboIssue makeDummyIssue() {
		return new TurboIssue(dummyRepoId, issues.size() + 1, "Issue " + (issues.size() + 1));
	}

	private TurboLabel makeDummyLabel() {
		return new TurboLabel(dummyRepoId, "Label " + (labels.size() + 1));
	}

	private TurboMilestone makeDummyMilestone() {
		return new TurboMilestone(dummyRepoId, milestones.size() + 1, "Milestone " + (milestones.size() + 1));
	}

	private TurboUser makeDummyUser() {
		return new TurboUser(dummyRepoId, "User " + (users.size() + 1));
	}

	protected List<TurboIssueEvent> getEvents() {
		return new ArrayList<>();
	}

	protected List<Comment> getComments() {
		return new ArrayList<>();
	}

	// UpdateEvent methods to directly mutate the repo state
	protected void makeNewIssue() {
		TurboIssue toAdd = makeDummyIssue();
		issues.add(toAdd);
		updatedIssues.add(toAdd);
	}

	protected void makeNewLabel() {
		TurboLabel toAdd = makeDummyLabel();
		labels.add(toAdd);
		updatedLabels.add(toAdd);
	}

	protected void makeNewMilestone() {
		TurboMilestone toAdd = makeDummyMilestone();
		milestones.add(toAdd);
		updatedMilestones.add(toAdd);
	}

	protected void makeNewUser() {
		TurboUser toAdd = makeDummyUser();
		users.add(toAdd);
		updatedUsers.add(toAdd);
	}

	// Only updating of issues and milestones is possible. Labels and users are immutable.
	protected TurboIssue updateIssue(int itemId, String updateText) {
		int toUpdate = 0;
		// Linear search
		for (TurboIssue issue : issues) {
			if (issue.getId() == itemId) break;
			toUpdate++;
		}
		if (toUpdate < issues.size()) { // Found
			TurboIssue issueToUpdate = issues.get(toUpdate);
			issueToUpdate.setTitle(updateText);
			updatedIssues.add(issueToUpdate);
			return issueToUpdate;
		}
		return null;
	}

	protected TurboMilestone updateMilestone(int itemId, String updateText) {
		int toUpdate = 0;
		// Linear search
		for (TurboMilestone milestone : milestones) {
			if (milestone.getId() == itemId) break;
			toUpdate++;
		}
		if (toUpdate < milestones.size()) { // Found
			TurboMilestone milestoneToUpdate = milestones.get(toUpdate);
			milestoneToUpdate.setTitle(updateText);
			updatedMilestones.add(milestoneToUpdate);
			return milestoneToUpdate;
		}
		return null;
	}

	protected TurboIssue deleteIssue(int itemId) {
		int toDelete = 0;
		for (TurboIssue issue : issues) {
			if (issue.getId() == itemId) break;
			toDelete++;
		}
		int toDeleteUpdated = 0;
		for (TurboIssue updatedIssue : updatedIssues) {
			if (updatedIssue.getId() == itemId) break;
			toDeleteUpdated++;
		}
		if (toDeleteUpdated < updatedIssues.size()) updatedIssues.remove(toDeleteUpdated);
		if (toDelete < issues.size()) { // Found
			return issues.remove(toDelete);
		}
		return null;
	}

	protected TurboLabel deleteLabel(String idString) {
		int toDelete = 0;
		for (TurboLabel label : labels) {
			if (label.getActualName().equalsIgnoreCase(idString)) break;
			toDelete++;
		}
		int toDeleteUpdated = 0;
		for (TurboLabel updatedLabel : updatedLabels) {
			if (updatedLabel.getActualName().equalsIgnoreCase(idString)) break;
			toDeleteUpdated++;
		}
		if (toDeleteUpdated < updatedLabels.size()) updatedLabels.remove(toDeleteUpdated);
		if (toDelete < labels.size()) { // Found
			return labels.remove(toDelete);
		}
		return null;
	}

	protected TurboMilestone deleteMilestone(int itemId) {
		int toDelete = 0;
		for (TurboMilestone milestone : milestones) {
			if (milestone.getId() == itemId) break;
			toDelete++;
		}
		int toDeleteUpdated = 0;
		for (TurboMilestone updatedMilestone : updatedMilestones) {
			if (updatedMilestone.getId() == itemId) break;
			toDeleteUpdated++;
		}
		if (toDeleteUpdated < updatedMilestones.size()) updatedMilestones.remove(toDeleteUpdated);
		if (toDelete < milestones.size()) { // Found
			return milestones.remove(toDelete);
		}
		return null;
	}

	protected TurboUser deleteUser(String idString) {
		int toDelete = 0;
		for (TurboUser user : users) {
			if (user.getLoginName().equalsIgnoreCase(idString)) break;
			toDelete++;
		}
		int toDeleteUpdated = 0;
		for (TurboUser updatedUser : updatedUsers) {
			if (updatedUser.getLoginName().equalsIgnoreCase(idString)) break;
			toDeleteUpdated++;
		}
		if (toDeleteUpdated < updatedUsers.size()) updatedUsers.remove(toDeleteUpdated);
		if (toDelete < users.size()) { // Found
			return users.remove(toDelete);
		}
		return null;
	}
}
