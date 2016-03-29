package backend.stub;

import backend.IssueMetadata;
import backend.UserCredentials;
import backend.github.*;
import backend.interfaces.RepoSource;
import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.egit.github.core.Issue;
import util.Futures;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DummySource extends RepoSource {

    private final DummyRepo dummy = new DummyRepo();

    @Override
    public String getName() {
        return "Dummy Source";
    }

    @Override
    public CompletableFuture<Boolean> login(UserCredentials credentials) {
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        execute(() -> response.complete(dummy.login(credentials)));
        return response;
    }

    @Override
    public CompletableFuture<Model> downloadRepository(String repoId) {
        return addTask(new DownloadRepoTask(this, dummy, repoId)).response;
    }

    @Override
    public CompletableFuture<GitHubModelUpdatesData> downloadModelUpdates(Model model) {
        return addTask(new DownloadModelUpdatesTask(this, dummy, model)).response;
    }

    @Override
    public CompletableFuture<Map<Integer, IssueMetadata>> downloadMetadata(String repoId,
                                                                           List<TurboIssue> issues) {
        return addTask(new DownloadMetadataTaskStub(this, dummy, repoId, issues)).response;
    }

    @Override
    public CompletableFuture<Boolean> replaceIssueLabels(TurboIssue issue, List<String> labels) {
        return addTask(new ReplaceIssueLabelsTask(this, dummy, issue.getRepoId(), issue.getId(), labels)).response;
    }

    @Override
    public CompletableFuture<Boolean> replaceIssueMilestone(TurboIssue issue, Optional<Integer> milestone) {
        return addTask(new ReplaceIssueMilestoneTask(this, dummy, issue.getRepoId(), issue.getId(), issue.getTitle(),
                                                     milestone)).response;
    }

    public CompletableFuture<Boolean> editIssueState(TurboIssue issue, boolean isOpen) {
        return addTask(new EditIssueStateTask(this, dummy, issue.getRepoId(), issue.getId(), isOpen)).response;
    }

    @Override
    public CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return Futures.unit(true);
    }

    @Override
    public CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime() {
        return addTask(new CheckRateLimitTask(this, dummy)).response;
    }

}
