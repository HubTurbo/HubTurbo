package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.time.LocalDateTime;

public class CheckRateLimitTask extends GitHubRepoTask<ImmutablePair<Integer, Long>> {

    public CheckRateLimitTask(TaskRunner taskRunner, Repo repo) {
        super(taskRunner, repo);
    }

    @Override
    public void run() {
        try {
            response.complete(repo.getRateLimitResetTime());
        } catch (IOException e) {
            response.completeExceptionally(e);
        }
    }
}
