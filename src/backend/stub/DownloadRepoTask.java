package backend.stub;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

public class DownloadRepoTask extends backend.github.DownloadRepoTask {

	public DownloadRepoTask(TaskRunner taskRunner, Repo<Issue> repo, String repoId) {
		super(taskRunner, repo, repoId);
	}
}
