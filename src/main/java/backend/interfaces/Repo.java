package backend.interfaces;

import backend.UserCredentials;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import backend.tupleresults.IntegerLongResult;
import backend.tupleresults.ListStringDateResult;
import backend.tupleresults.ListStringResult;
import github.ReviewComment;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface Repo {

    boolean login(UserCredentials credentials);

    List<TurboIssue> getIssues(String repoId);
    List<TurboLabel> getLabels(String repoId);
    List<TurboMilestone> getMilestones(String repoId);
    List<TurboUser> getCollaborators(String repoId);

    // Returns tuples in order to be maximally generic
    ListStringDateResult getUpdatedIssues(String repoId, String eTag, Date lastCheckTime);
    List<PullRequest> getUpdatedPullRequests(String repoId, Date lastCheckTime);
    ListStringResult getUpdatedLabels(String repoId, String eTag);
    ListStringResult getUpdatedMilestones(String repoId, String eTag);
    ListStringResult getUpdatedCollaborators(String repoId, String eTag);

    ListStringResult getUpdatedEvents(String repoId, int issueId, String eTag);
    List<Comment> getComments(String repoId, int issueId);
    List<ReviewComment> getReviewComments(String repoId, int pullRequestId);
    List<Comment> getAllComments(String repoId, TurboIssue issue);

    boolean isRepositoryValid(String repoId);
    List<Label> setLabels(String repoId, int issueId, List<String> labels) throws IOException;
    IntegerLongResult getRateLimitResetTime() throws IOException;

}
