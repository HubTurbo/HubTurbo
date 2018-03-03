package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;

import java.io.IOException;

public class CheckRateLimitTask extends GitHubRepoTask<ApiQuotaInfo> {

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
