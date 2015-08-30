package backend.stub;

import backend.github.DownloadRepoTask;
import backend.interfaces.TaskRunner;

public class DownloadRepoTaskStub extends DownloadRepoTask {

    public DownloadRepoTaskStub(TaskRunner taskRunner, DummyRepo repo, String repoId) {
        super(taskRunner, repo, repoId);
    }
}
