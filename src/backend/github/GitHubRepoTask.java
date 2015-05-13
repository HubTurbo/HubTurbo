package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.BlockingQueue;

public abstract class GitHubRepoTask<R> extends RepoTask<R, Issue> {

	public GitHubRepoTask(BlockingQueue<RepoTask<?, ?>> tasks, Repo<Issue> repo) {
		super(tasks, repo);
	}
}
