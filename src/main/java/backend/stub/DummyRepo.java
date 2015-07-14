package backend.stub;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Comment;

import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.TurboIssueEvent;
import ui.UI;
import util.events.testevents.ClearLogicModelEvent;
import util.events.testevents.UpdateDummyRepoEventHandler;

public class DummyRepo implements Repo {

    private final HashMap<String, DummyRepoState> repoStates = new HashMap<>();

    public DummyRepo() {
        if (UI.events == null) {
            // UI isn't initialised
            return;
        }
        UI.events.registerEvent((UpdateDummyRepoEventHandler) e -> {
            assert e.repoId != null;
            switch (e.updateType) {
                case NEW_ISSUE:
                    getRepoState(e.repoId).makeNewIssue();
                    break;
                case NEW_LABEL:
                    getRepoState(e.repoId).makeNewLabel();
                    break;
                case NEW_MILESTONE:
                    getRepoState(e.repoId).makeNewMilestone();
                    break;
                case NEW_USER:
                    getRepoState(e.repoId).makeNewUser();
                    break;
                // TODO implement update of issue and milestone
                // (after switching to TreeMap implementation)
                case UPDATE_ISSUE:
                    getRepoState(e.repoId).updateIssue(e.itemId, e.updateText);
                    break;
                case UPDATE_MILESTONE:
                    getRepoState(e.repoId).updateMilestone(e.itemId, e.updateText);
                    break;
                // Model reload is done by event handler registered in Logic in
                // the following five:
                case DELETE_ISSUE:
                    getRepoState(e.repoId).deleteIssue(e.itemId);
                    UI.events.triggerEvent(new ClearLogicModelEvent(e.repoId));
                    break;
                case DELETE_LABEL:
                    getRepoState(e.repoId).deleteLabel(e.idString);
                    UI.events.triggerEvent(new ClearLogicModelEvent(e.repoId));
                    break;
                case DELETE_MILESTONE:
                    getRepoState(e.repoId).deleteMilestone(e.itemId);
                    UI.events.triggerEvent(new ClearLogicModelEvent(e.repoId));
                    break;
                case DELETE_USER:
                    getRepoState(e.repoId).deleteUser(e.idString);
                    UI.events.triggerEvent(new ClearLogicModelEvent(e.repoId));
                    break;
                case RESET_REPO:
                    repoStates.put(e.repoId, new DummyRepoState(e.repoId));
                    UI.events.triggerEvent(new ClearLogicModelEvent(e.repoId));
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
        getUpdatedIssues(String repoId, String eTag, Date lastCheckTime) {
        return getRepoState(repoId).getUpdatedIssues(eTag, lastCheckTime);
    }

    @Override
    public ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String repoId, String eTag) {
        return getRepoState(repoId).getUpdatedLabels(eTag);
    }

    @Override
    public ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String repoId, String eTag) {
        return getRepoState(repoId).getUpdatedMilestones(eTag);
    }

    @Override
    public ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String repoId, String eTag) {
        return getRepoState(repoId).getUpdatedCollaborators(eTag);
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
        return getRepoState(repoId).getEvents(issueId);
    }

    @Override
    public List<Comment> getComments(String repoId, int issueId) {
        return getRepoState(repoId).getComments(issueId);
    }

    @Override
    public boolean isRepositoryValid(String repoId) {
        return true;
    }

    /**
     * Presents reasonable defaults to the user.
     *
     * @return 3500 remaining calls, reset time ~45 minutes (27000000 milliseconds) from call.
     */
    @Override
    public ImmutablePair<Integer, Long> getRateLimitResetTime() {
        return new ImmutablePair<>(3500, new Date().getTime() + 2700000);
    }
}
