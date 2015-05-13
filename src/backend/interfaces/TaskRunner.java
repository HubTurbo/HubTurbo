package backend.interfaces;

public interface TaskRunner {
	public <R, I> RepoTask<R, I> addTask(RepoTask<R, I> task);
	public void execute(Runnable r);
}
