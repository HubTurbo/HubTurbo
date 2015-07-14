package backend.interfaces;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Comment;

import backend.UserCredentials;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.TurboIssueEvent;

public interface Repo {

    boolean login(UserCredentials credentials);

    List<TurboIssue> getIssues(String repoId);
    List<TurboLabel> getLabels(String repoId);
    List<TurboMilestone> getMilestones(String repoId);
    List<TurboUser> getCollaborators(String repoId);

    // Returns tuples in order to be maximally generic
    ImmutableTriple<List<TurboIssue>, String, Date>
        getUpdatedIssues(String repoId, String eTag, Date lastCheckTime);
    ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String repoId, String eTag);
    ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String repoId, String eTag);
    ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String repoId, String eTag);

    List<TurboIssueEvent> getEvents(String repoId, int issueId);
    List<Comment> getComments(String repoId, int issueId);

    boolean isRepositoryValid(String repoId);
    ImmutablePair<Integer, Long> getRateLimitResetTime() throws IOException;
}
