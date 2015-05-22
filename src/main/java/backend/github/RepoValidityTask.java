package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

public class RepoValidityTask extends GitHubRepoTask<Boolean> {

	private final String repoId;

	public RepoValidityTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, String repoId) {
		super(taskRunner, repo);
		this.repoId = repoId;
	}

	@Override
	public void run() {
		response.complete(repo.isRepositoryValid(repoId));
	}
}
