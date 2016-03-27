package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;

import java.io.IOException;

/**
 * This class represents an async task that edits the state of an issue on GitHub.
 */
public class EditIssueStateTask extends GitHubRepoTask<Boolean> {

    private final String repoId;
    private final int issueId;
    private final boolean isOpen;

    public EditIssueStateTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId, boolean isOpen) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.isOpen = isOpen;
    }

    @Override
    public void run() {
        try {
            response.complete(repo.editIssueState(repoId, issueId, isOpen));
        } catch (IOException e) {
            response.complete(false);
        }
    }
}
