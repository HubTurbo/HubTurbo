package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

import java.io.IOException;

public class ReplaceIssueAssigneeTask extends GitHubRepoTask<Boolean> {
    private final String repoId;
    private final int issueId;
    private final String issueTitle;
    private final String issueAssigneeLoginName;

    public ReplaceIssueAssigneeTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId,
                                    String issueTitle, String issueAssigneeLoginName) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.issueTitle = issueTitle;
        this.issueAssigneeLoginName = issueAssigneeLoginName;
    }

    @Override
    public void run() {
        try {
            Issue result = repo.setAssignee(repoId, issueId, issueTitle, issueAssigneeLoginName);
            String resultAssigneeLoginName = result.getAssignee().getLogin();

            if (resultAssigneeLoginName == null) {
                response.complete(issueAssigneeLoginName == null);
            } else {
                response.complete(result.getAssignee().getLogin().equals(issueAssigneeLoginName));
            }
        } catch (IOException e) {
            response.completeExceptionally(e);
        }
    }
}
