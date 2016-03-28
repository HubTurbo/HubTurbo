package backend.stub;

import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.ReviewComment;
import github.TurboIssueEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.*;
import ui.UI;
import util.events.testevents.ClearLogicModelEvent;
import util.events.testevents.UpdateDummyRepoEventHandler;

import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DummyRepo implements Repo {

    private final HashMap<String, DummyRepoState> repoStates = new HashMap<>();

    // Only decreases after API retrievals
    private int apiQuota = 3500;

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
            case ADD_COMMENT:
                getRepoState(e.repoId).commentOnIssue(e.actor, e.updateText, e.itemId);
                break;
            case RESET_REPO:
                repoStates.put(e.repoId, new DummyRepoState(e.repoId));
                UI.events.triggerEvent(new ClearLogicModelEvent(e.repoId));
                break;
            default:
                assert false : "Missing case " + e.updateType;
                break;
            }
        });
    }

    @Override
    public boolean login(UserCredentials credentials) {
        return credentials.username.equals("test") && credentials.password.equals("test");
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
    public List<PullRequest> getUpdatedPullRequests(String repoId, Date lastCheckTime) {
        return new ArrayList<>();
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
    public ImmutablePair<List<TurboIssueEvent>, String>
            getUpdatedEvents(String repoId, int issueId, String currentETag) {

        ImmutablePair<List<TurboIssueEvent>, String> result = getRepoState(repoId).getEvents(issueId, currentETag);

        if (!result.getRight().equals(currentETag) || currentETag.length() == 0) apiQuota--;

        return result;
    }

    @Override
    public List<Comment> getComments(String repoId, int issueId) {
        apiQuota--;
        return getRepoState(repoId).getComments(issueId);
    }

    @Override
    public List<Comment> getAllComments(String repoId, TurboIssue issue) {
        List<Comment> result = getComments(repoId, issue.getId());
        result.addAll(getReviewComments(repoId, issue.getId()));
        return result;
    }

    @Override
    public List<ReviewComment> getReviewComments(String repoId, int pullRequestId) {
        return new ArrayList<>();
    }

    @Override
    public List<Label> setLabels(String repoId, int issueId, List<String> labels) {
        return getRepoState(repoId).setLabels(issueId, labels);
    }

    @Override
    public Optional<Integer> setMilestone(String repoId, int issueId, String issueTitle,
                                          Optional<Integer> issueMilestone) {
        Issue returnedIssue = getRepoState(repoId).setMilestone(issueId, issueMilestone);
        return Optional.ofNullable(returnedIssue.getMilestone())
                .map(Milestone::getNumber);
    }

    public boolean editIssueState(String repoId, int issueId, boolean isOpen) throws IOException {
        return getRepoState(repoId).editIssueState(issueId, isOpen);
    }

    @Override
    public boolean isRepositoryValid(String repoId) {
        return true;
    }

    /**
     * Presents reasonable default for reset time, as well as the API quota, to the user.
     * The API quota only decreases when retrieving metadata from an issue updated since last retrieval.
     *
     * @return Remaining calls, reset time ~45 minutes (27000000 milliseconds) from call.
     */
    @Override
    public ImmutablePair<Integer, Long> getRateLimitResetTime() {
        return new ImmutablePair<>(apiQuota, new Date().getTime() + 2700000);
    }

}
