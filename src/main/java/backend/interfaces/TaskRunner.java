package backend.interfaces;

public interface TaskRunner {
	public <R, I, L, M, U> RepoTask<R, I, L, M, U> addTask(RepoTask<R, I, L, M, U> task);
	public void execute(Runnable r);
}
