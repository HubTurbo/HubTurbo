package backend.updates.github;

import backend.interfaces.Repo;
import backend.updates.RepoTask;

import java.util.concurrent.BlockingQueue;

public abstract class GHTask<R> extends RepoTask<R> {

	public GHTask(BlockingQueue<RepoTask<?>> tasks, Repo repo) {
		super(tasks, repo);
	}
}
