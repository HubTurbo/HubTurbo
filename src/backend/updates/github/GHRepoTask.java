package backend.updates.github;

import backend.interfaces.Repo;
import backend.updates.RepoTask;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.BlockingQueue;

public abstract class GHRepoTask<R> extends RepoTask<R, Issue> {

	public GHRepoTask(BlockingQueue<RepoTask<?, ?>> tasks, Repo<Issue> repo) {
		super(tasks, repo);
	}
}
