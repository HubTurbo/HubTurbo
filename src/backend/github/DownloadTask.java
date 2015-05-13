package backend.github;

import backend.Model;
import backend.TurboIssue;
import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

class DownloadTask extends GitHubRepoTask<Model> {

	private final String repoId;

	public DownloadTask(TaskRunner taskRunner, Repo<Issue> repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		List<TurboIssue> issues = repo.getIssues(repoId).stream()
			.map(TurboIssue::new)
			.collect(Collectors.toList());
		Model result = new Model(RepositoryId.createFromId(repoId), issues, UpdateSignature.empty);
		response.complete(result);
	}
}
