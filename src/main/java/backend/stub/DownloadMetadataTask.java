package backend.stub;

import java.util.List;

import backend.interfaces.TaskRunner;

public class DownloadMetadataTask extends backend.github.DownloadMetadataTask {

	public DownloadMetadataTask(TaskRunner taskRunner, DummyRepo repo, String repoId, List<Integer> issueIds) {
		super(taskRunner, repo, repoId, issueIds);
	}
}
