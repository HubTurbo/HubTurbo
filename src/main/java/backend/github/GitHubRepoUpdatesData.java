package backend.github;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import org.eclipse.egit.github.core.PullRequest;

import java.util.List;

/**
 * This classes stores the updates data for a repository downloaded from GitHub
 */
public final class GitHubRepoUpdatesData {
    private final String repoId;
    private final GitHubRepoTask.Result<TurboIssue> issues;
    private final List<PullRequest> pullRequests;
    private final GitHubRepoTask.Result<TurboLabel> labels;
    private final GitHubRepoTask.Result<TurboMilestone> milestones;
    private final GitHubRepoTask.Result<TurboUser> users;

    public GitHubRepoUpdatesData(String repoId,
                                 GitHubRepoTask.Result<TurboIssue> issues, List<PullRequest> pullRequests,
                                 GitHubRepoTask.Result<TurboLabel> labels,
                                 GitHubRepoTask.Result<TurboMilestone> milestones,
                                 GitHubRepoTask.Result<TurboUser> users) {
        this.repoId = repoId;
        this.issues = issues;
        this.pullRequests = pullRequests;
        this.labels = labels;
        this.milestones = milestones;
        this.users = users;
    }

    public String getRepoId() {
        return repoId;
    }

    public GitHubRepoTask.Result<TurboIssue> getIssues() {
        return issues;
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public GitHubRepoTask.Result<TurboLabel> getLabels() {
        return labels;
    }

    public GitHubRepoTask.Result<TurboMilestone> getMilestones() {
        return milestones;
    }

    public GitHubRepoTask.Result<TurboUser> getUsers() {
        return users;
    }
}
