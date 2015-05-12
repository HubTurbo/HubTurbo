package backend.updates;

import backend.interfaces.Repo;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.BlockingQueue;

public abstract class RepoTask {
	public final BlockingQueue<RepoTask> tasks;
	public final Repo repo;

	public RepoTask(BlockingQueue<RepoTask> tasks, Repo repo) {
		this.tasks = tasks;
		this.repo = repo;
	}

	public abstract void update();
}
