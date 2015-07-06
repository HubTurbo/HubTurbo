package backend.stub;

import backend.interfaces.TaskRunner;

import java.util.List;

public class ReplaceIssueLabelsTask extends backend.github.ReplaceIssueLabelsTask {

    public ReplaceIssueLabelsTask(TaskRunner taskRunner, DummyRepo repo, String repoId, int issueId,
                                  List<String> labels) {
        super(taskRunner, repo, repoId, issueId, labels);
    }

}