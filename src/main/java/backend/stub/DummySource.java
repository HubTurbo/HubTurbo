package backend.stub;

import backend.IssueMetadata;
import backend.UserCredentials;
import backend.interfaces.RepoSource;
import backend.resource.Model;
import org.apache.commons.lang3.tuple.ImmutablePair;
import util.Futures;

import java.util.List;
import java.util.Map;
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
    public CompletableFuture<Model> updateModel(Model model) {
        return addTask(new UpdateModelTask(this, dummy, model)).response;
    }

    @Override
    public CompletableFuture<Map<Integer, IssueMetadata>> downloadMetadata(String repoId, List<Integer> issues) {
        return addTask(new DownloadMetadataTask(this, dummy, repoId, issues)).response;
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
