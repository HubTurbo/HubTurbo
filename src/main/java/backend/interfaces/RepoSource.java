package backend.interfaces;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import backend.IssueMetadata;
import backend.UserCredentials;
import backend.resource.Model;
import org.apache.commons.lang3.tuple.ImmutablePair;

public abstract class RepoSource implements TaskRunner {

    private final ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public <R> RepoTask<R> addTask(RepoTask<R> task) {
        execute(task);
        return task;
    }

    @Override
    public void execute(Runnable r) {
        pool.execute(r);
    }

    public abstract String getName();

    public abstract CompletableFuture<Boolean> login(UserCredentials credentials);

    public abstract CompletableFuture<Model> downloadRepository(String repoId);

    public abstract CompletableFuture<Model> updateModel(Model model);

    public abstract CompletableFuture<Map<Integer, IssueMetadata>>
        downloadMetadata(String repoId, List<Integer> issues);

    public abstract CompletableFuture<Boolean> isRepositoryValid(String repoId);

    public abstract CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime();

}
