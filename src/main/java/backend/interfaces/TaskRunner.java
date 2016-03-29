package backend.interfaces;

public interface TaskRunner {
    <R> RepoTask<R> addTask(RepoTask<R> task);

    void execute(Runnable r);
}
