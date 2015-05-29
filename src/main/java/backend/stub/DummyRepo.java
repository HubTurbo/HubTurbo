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
		UI.events.registerEvent((UpdateDummyRepoEventHandler) e -> {
			assert e.repoId != null;
			switch (e.updateType) {
				case NEW_ISSUE:
					getRepoState(e.repoId).makeNewIssue();
					break;
//				case NEW_LABEL:
//					getRepoState(e.repoId).makeNewLabel();
//					break;
//				case NEW_MILESTONE:
//					getRepoState(e.repoId).makeNewMilestone();
//					break;
//				case NEW_USER:
//					getRepoState(e.repoId).makeNewUser();
//					break;
//				case UPDATE_ISSUE:
//					getRepoState(e.repoId).updateIssue(e.itemId, e.updateText);
//					break;
//				case UPDATE_LABEL:
//					getRepoState(e.repoId).updateLabel(e.itemId, e.updateText);
//					break;
//				case UPDATE_MILESTONE:
//					getRepoState(e.repoId).updateMilestone(e.itemId, e.updateText);
//					break;
//				case UPDATE_USER:
//					getRepoState(e.repoId).updateUser(e.itemId, e.updateText);
//					break;
//				case DELETE_ISSUE:
//					getRepoState(e.repoId).deleteIssue();
//					break;
//				case DELETE_LABEL:
//					getRepoState(e.repoId).deleteLabel();
//					break;
//				case DELETE_MILESTONE:
//					getRepoState(e.repoId).deleteMilestone();
//					break;
//				case DELETE_USER:
//					getRepoState(e.repoId).deleteUser();
//					break;
				case RESET_REPO:
					// Model reload is done by event handler registered in Logic
					repoStates.put(e.repoId, new DummyRepoState(e.repoId));
					break;
			}
		});
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
