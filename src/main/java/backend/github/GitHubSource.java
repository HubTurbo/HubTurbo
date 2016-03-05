package backend.github;

import backend.IssueMetadata;
import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.interfaces.RepoSource;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GitHubSource extends RepoSource {

    private static final Logger logger = HTLog.get(GitHubSource.class);

    private final Repo gitHub = new GitHubRepo();

    @Override
    public String getName() {
        return "GitHub";
    }

    @Override
    public CompletableFuture<Boolean> login(UserCredentials credentials) {
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        execute(() -> {
            boolean success = gitHub.login(credentials);
            logger.info(String.format("%s to %s as %s",
                success ? "Logged in" : "Failed to log in",
                getName(), credentials.username));
            response.complete(success);
        });

        return response;
    }

    @Override
    public CompletableFuture<Model> downloadRepository(String repoId) {
        return addTask(new DownloadRepoTask(this, gitHub, repoId)).response;
    }

    @Override
    public CompletableFuture<GitHubRepoUpdatesData> downloadModelUpdates(Model model) {
        return addTask(new DownloadModelUpdatesTask(this, gitHub, model)).response;
    }

    @Override
    public CompletableFuture<Map<Integer, IssueMetadata>> downloadMetadata(String repoId, List<TurboIssue> issues) {
        return addTask(new DownloadMetadataTask(this, gitHub, repoId, issues)).response;
    }

    @Override
    public CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return addTask(new RepoValidityTask(this, gitHub, repoId)).response;
    }

    @Override
    public CompletableFuture<Boolean> replaceIssueLabels(TurboIssue issue, List<String> labels) {
        return addTask(new ReplaceIssueLabelsTask(this, gitHub, issue.getRepoId(), issue.getId(), labels)).response;
    }

@Override
    public CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime() {
        return addTask(new CheckRateLimitTask(this, gitHub)).response;
    }

}
