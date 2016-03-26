package backend.github;

import backend.resource.*;
import org.eclipse.egit.github.core.PullRequest;

import java.util.List;

/**
 * This classes stores the updates data downloaded from GitHub for a repository represented locally as a Model
 */
public final class GitHubModelUpdatesData {
    private final Model model;
    private final GitHubRepoTask.Result<TurboIssue> issues;
    private final List<PullRequest> pullRequests;
    private final GitHubRepoTask.Result<TurboLabel> labels;
    private final GitHubRepoTask.Result<TurboMilestone> milestones;
    private final GitHubRepoTask.Result<TurboUser> users;

    public GitHubModelUpdatesData(Model model,
                                  GitHubRepoTask.Result<TurboIssue> issues, List<PullRequest> pullRequests,
                                  GitHubRepoTask.Result<TurboLabel> labels,
                                  GitHubRepoTask.Result<TurboMilestone> milestones,
                                  GitHubRepoTask.Result<TurboUser> users) {
        this.model = model;
        this.issues = issues;
        this.pullRequests = pullRequests;
        this.labels = labels;
        this.milestones = milestones;
        this.users = users;
    }

    public String getRepoId() {
        return model.getRepoId();
    }

    public Model getModel() {
        return model;
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
