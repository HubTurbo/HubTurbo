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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DummyRepo implements Repo {

	private static final HashMap<String, DummyRepoState> repoStates = new HashMap<>();

	public DummyRepo() {
//		UI.events.registerEvent((UpdateDummyRepoEventHandler) e -> {
//			switch (e.updateType) {
//				case NEW_ISSUE:
//					issues.add(makeDummyIssue(DUMMY_REPO_ID));
//					break;
//			}
//		});
	}

	@Override
	public boolean login(UserCredentials credentials) {
		if (credentials.username.equals("test") && credentials.password.equals("test")) return true;
		return false;
	}

	private DummyRepoState getRepoState(String repoId) {
		DummyRepoState repoToGet = repoStates.get(repoId);
		if (repoToGet == null) {
			repoToGet = new DummyRepoState(repoId);
			repoStates.put(repoId, repoToGet);
		}
		return repoToGet;
	}

	@Override
	public ImmutableTriple<List<TurboIssue>, String, Date>
		getUpdatedIssues(String repoId, String ETag, Date lastCheckTime) {
		return getRepoState(repoId).getUpdatedIssues(ETag, lastCheckTime);
	}

	@Override
	public ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String repoId, String ETag) {
		return getRepoState(repoId).getUpdatedLabels(ETag);
	}

	@Override
	public ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String repoId, String ETag) {
		return getRepoState(repoId).getUpdatedMilestones(ETag);
	}

	@Override
	public ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String repoId, String ETag) {
		return getRepoState(repoId).getUpdatedCollaborators(ETag);
	}

	@Override
	public List<TurboIssue> getIssues(String repoId) {
		return getRepoState(repoId).getIssues();
	}

	@Override
	public List<TurboLabel> getLabels(String repoId) {
		return getRepoState(repoId).getLabels();
	}

	@Override
	public List<TurboMilestone> getMilestones(String repoId) {
		return getRepoState(repoId).getMilestones();
	}

	@Override
	public List<TurboUser> getCollaborators(String repoId) {
		return getRepoState(repoId).getCollaborators();
	}

	@Override
	public List<TurboIssueEvent> getEvents(String repoId, int issueId) {
		return getRepoState(repoId).getEvents();
	}

	@Override
	public List<Comment> getComments(String repoId, int issueId) {
		return getRepoState(repoId).getComments();
	}

	@Override
	public boolean isRepositoryValid(String repoId) {
		return true;
	}

}
