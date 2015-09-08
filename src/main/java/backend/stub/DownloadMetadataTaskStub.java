package backend.stub;

import backend.github.DownloadMetadataTask;
import backend.interfaces.TaskRunner;
import backend.resource.TurboIssue;

import java.util.List;
import java.util.Map;

public class DownloadMetadataTaskStub extends DownloadMetadataTask {

    public DownloadMetadataTaskStub(TaskRunner taskRunner,
                                    DummyRepo repo,
                                    String repoId,
                                    List<TurboIssue> issuesToUpdate) {
        super(taskRunner, repo, repoId, issuesToUpdate);
    }
}
