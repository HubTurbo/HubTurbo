package backend.stub;

import backend.interfaces.TaskRunner;

import java.util.List;
import java.util.Map;

public class DownloadMetadataTask extends backend.github.DownloadMetadataTask {

    public DownloadMetadataTask(TaskRunner taskRunner, DummyRepo repo, String repoId, Map<Integer, String> issueIds) {
        super(taskRunner, repo, repoId, issueIds);
    }
}
