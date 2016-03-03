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

            if (result.getMilestone() == null) {
                response.complete(issueMilestone == null);
            } else {
                response.complete(result.getMilestone().getNumber() == issueMilestone);
            }
        } catch (IOException e) {
            response.completeExceptionally(e);
        }
    }
}
