package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

import java.io.IOException;
import java.util.Optional;

public class ReplaceIssueAssigneeTask extends GitHubRepoTask<Boolean> {
    private final String repoId;
    private final int issueId;
    private final String issueTitle;
    private final Optional<String> issueAssigneeLoginName;

    public ReplaceIssueAssigneeTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId,
                                    String issueTitle, Optional<String> issueAssigneeLoginName) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.issueTitle = issueTitle;
        this.issueAssigneeLoginName = issueAssigneeLoginName;
    }

    @Override
    public void run() {
        Optional<String> result;
        try {
            result = repo.setAssignee(repoId, issueId, issueTitle, issueAssigneeLoginName);
        } catch (IOException e) {
            response.completeExceptionally(e);
            return;
        }
        response.complete(issueAssigneeLoginName.equals(result));
    }
}
