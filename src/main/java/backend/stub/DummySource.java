package backend.stub;

import backend.IssueMetadata;
import backend.UserCredentials;
import backend.interfaces.RepoSource;
import backend.resource.Model;
import util.Utility;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DummySource extends RepoSource {

	private final DummyRepo dummy = new DummyRepo();

	@Override
	public String getName() {
		return "Dummy Repository";
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
		// TODO
		assert false : "Not yet implemented";
		return null;
	}

	@Override
	public CompletableFuture<Boolean> isRepositoryValid(String repoId) {
		return Utility.unitFutureOf(true);
	}
}
