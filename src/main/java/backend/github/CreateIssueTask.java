package backend.github;

import java.io.IOException;

import org.eclipse.egit.github.core.Issue;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.TurboIssue;

public class CreateIssueTask extends GitHubRepoTask<Issue> {

    private TurboIssue issue;

    public CreateIssueTask(TaskRunner taskRunner, Repo repo, TurboIssue issue) {
        super(taskRunner, repo);
        this.issue = issue;
    }

    @Override
    public void run() {
        try {
            response.complete(repo.createIssue(issue));
        } catch (IOException e) {
            response.completeExceptionally(e);
        }
    }

}
