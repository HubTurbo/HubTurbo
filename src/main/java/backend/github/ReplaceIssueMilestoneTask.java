package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

import java.io.IOException;
import java.util.Optional;

public class ReplaceIssueMilestoneTask extends GitHubRepoTask<Boolean> {
    private final String repoId;
    private final int issueId;
    private final String issueTitle;
    private final Optional<Integer> issueMilestone;

    public ReplaceIssueMilestoneTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId, String issueTitle,
                                     Optional<Integer> issueMilestone) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.issueMilestone = issueMilestone;
        this.issueTitle = issueTitle;
    }

    @Override
    public void run() {
        Optional<Integer> result;
        try {
            result = repo.setMilestone(repoId, issueId, issueTitle, issueMilestone);
        } catch (IOException e) {
            response.completeExceptionally(e);
            return;
        }

        response.complete(issueMilestone.equals(result));
    }
}
