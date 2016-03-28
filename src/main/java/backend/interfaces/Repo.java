package backend.interfaces;

import backend.UserCredentials;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.ReviewComment;
import github.TurboIssueEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface Repo {

    boolean login(UserCredentials credentials);

    List<TurboIssue> getIssues(String repoId);

    List<TurboLabel> getLabels(String repoId);

    List<TurboMilestone> getMilestones(String repoId);

    List<TurboUser> getCollaborators(String repoId);

    // Returns tuples in order to be maximally generic
    ImmutableTriple<List<TurboIssue>, String, Date> getUpdatedIssues(String repoId, String eTag, Date lastCheckTime);

    List<PullRequest> getUpdatedPullRequests(String repoId, Date lastCheckTime);

    ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String repoId, String eTag);

    ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String repoId, String eTag);

    ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String repoId, String eTag);

    ImmutablePair<List<TurboIssueEvent>, String> getUpdatedEvents(String repoId, int issueId, String eTag);

    List<Comment> getComments(String repoId, int issueId);

    List<ReviewComment> getReviewComments(String repoId, int pullRequestId);

    List<Comment> getAllComments(String repoId, TurboIssue issue);

    boolean isRepositoryValid(String repoId);

    List<Label> setLabels(String repoId, int issueId, List<String> labels) throws IOException;

    Optional<Integer> setMilestone(String repoId, int issueId, String issueTitle, Optional<Integer> issueMilestone)
            throws IOException;

    boolean editIssueState(String repoId, int issueId, boolean isOpen) throws IOException;

    ImmutablePair<Integer, Long> getRateLimitResetTime() throws IOException;

}
