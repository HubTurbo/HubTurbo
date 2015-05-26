package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;

public class RepoValidityTask extends GitHubRepoTask<Boolean> {

	private final String repoId;

	public RepoValidityTask(TaskRunner taskRunner, Repo repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		response.complete(repo.isRepositoryValid(repoId));
	}
}
