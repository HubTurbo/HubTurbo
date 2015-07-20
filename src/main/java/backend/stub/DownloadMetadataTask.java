package backend.stub;

import backend.interfaces.TaskRunner;

import java.util.List;

public class DownloadMetadataTask extends backend.github.DownloadMetadataTask {

    public DownloadMetadataTask(TaskRunner taskRunner, DummyRepo repo, String repoId, List<Integer> issueIds) {
        super(taskRunner, repo, repoId, issueIds);
    }
}
