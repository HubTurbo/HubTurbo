package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Label;

import java.util.List;
import java.util.stream.Collectors;

public class ReplaceIssueLabelsTask extends GitHubRepoTask<List<String>> {

    private final String repoId;
    private final int issueId;
    private final List<String> labels;

    public ReplaceIssueLabelsTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId, List<String> labels) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.labels = labels;
    }

    @Override
    public void run() {
        response.complete(
                repo.setLabels(repoId, issueId, labels).stream().map(Label::getName).collect(Collectors.toList())
        );
    }
}