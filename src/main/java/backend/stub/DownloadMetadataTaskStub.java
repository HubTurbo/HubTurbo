package backend.stub;

import backend.github.DownloadMetadataTask;
import backend.interfaces.TaskRunner;

import java.util.List;

public class DownloadMetadataTaskStub extends DownloadMetadataTask {

    public DownloadMetadataTaskStub(TaskRunner taskRunner, DummyRepo repo, String repoId, List<Integer> issueIds) {
        super(taskRunner, repo, repoId, issueIds);
    }
}
