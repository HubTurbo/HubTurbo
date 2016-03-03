package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

import java.io.IOException;

public class ReplaceIssueMilestoneTask extends GitHubRepoTask<Boolean> {
    private final String repoId;
    private final int issueId;
    private final String issueTitle;
    private final Integer issueMilestone;

    public ReplaceIssueMilestoneTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId, String issueTitle,
                                     Integer issueMilestone) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.issueMilestone = issueMilestone;
        this.issueTitle = issueTitle;
    }

    @Override
    public void run() {
        try {
            Issue result = repo.setMilestone(repoId, issueId, issueTitle, issueMilestone);
            Integer milestoneNumber = result.getMilestone() == null ? null : result.getMilestone().getNumber();
            if (milestoneNumber == issueMilestone) response.complete(true);
            if (milestoneNumber == null || issueMilestone == null) response.complete(false);
            response.complete(milestoneNumber.equals(issueMilestone));
        } catch (IOException e) {
            response.completeExceptionally(e);
        }
    }
}
