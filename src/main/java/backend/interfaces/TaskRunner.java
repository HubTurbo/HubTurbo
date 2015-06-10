package backend.interfaces;

public interface TaskRunner {
	public <R> RepoTask<R> addTask(RepoTask<R> task);
	public void execute(Runnable r);
}
