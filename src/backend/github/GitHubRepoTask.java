package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.BlockingQueue;

public abstract class GitHubRepoTask<R> extends RepoTask<R, Issue> {

	public GitHubRepoTask(TaskRunner taskRunner, Repo<Issue> repo) {
		super(taskRunner, repo);
	}
}
