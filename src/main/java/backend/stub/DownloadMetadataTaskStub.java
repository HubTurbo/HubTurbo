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

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            assert false;
        }
        super.run();
    }
}
