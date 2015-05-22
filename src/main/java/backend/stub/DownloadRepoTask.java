package backend.stub;

import backend.interfaces.TaskRunner;

public class DownloadRepoTask extends backend.github.DownloadRepoTask {

	public DownloadRepoTask(TaskRunner taskRunner, DummyRepo repo, String repoId) {
		super(taskRunner, repo, repoId);
	}
}
