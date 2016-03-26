package backend.interfaces;

import backend.IssueMetadata;
import backend.UserCredentials;
import backend.github.GitHubModelUpdatesData;
import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.egit.github.core.Issue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public abstract CompletableFuture<GitHubModelUpdatesData> downloadModelUpdates(Model model);

    public abstract CompletableFuture<Map<Integer, IssueMetadata>> downloadMetadata(String repoId,
                                                                                    List<TurboIssue> issues);

    public abstract CompletableFuture<Boolean> isRepositoryValid(String repoId);

    public abstract CompletableFuture<Boolean> replaceIssueLabels(TurboIssue issue, List<String> labels);

    public abstract CompletableFuture<Boolean> replaceIssueMilestone(TurboIssue issue, Optional<Integer> milestone);

    public abstract CompletableFuture<Boolean> editIssueState(TurboIssue issue, boolean isOpen);

    public abstract CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime();

}
