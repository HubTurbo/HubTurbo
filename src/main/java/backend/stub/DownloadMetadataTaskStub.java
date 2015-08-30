package backend.stub;

import backend.github.DownloadMetadataTask;
import backend.interfaces.TaskRunner;

import java.util.List;
import java.util.Map;

public class DownloadMetadataTaskStub extends DownloadMetadataTask {

    public DownloadMetadataTaskStub(TaskRunner taskRunner,
                                    DummyRepo repo,
                                    String repoId,
                                    Map<Integer, String> issueIds) {
        super(taskRunner, repo, repoId, issueIds);
    }
}
