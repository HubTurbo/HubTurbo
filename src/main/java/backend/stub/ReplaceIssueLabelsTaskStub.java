package backend.stub;

import backend.github.ReplaceIssueLabelsTask;
import backend.interfaces.TaskRunner;

import java.util.List;

public class ReplaceIssueLabelsTaskStub extends ReplaceIssueLabelsTask {

    public ReplaceIssueLabelsTaskStub(TaskRunner taskRunner, DummyRepo repo, String repoId, int issueId,
                                      List<String> labels) {
        super(taskRunner, repo, repoId, issueId, labels);
    }

}
