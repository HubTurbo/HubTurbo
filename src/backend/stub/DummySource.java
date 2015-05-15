package backend.stub;

import backend.resource.Model;
import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.interfaces.RepoSource;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.CompletableFuture;

public class DummySource extends RepoSource {

	private final Repo<Issue> dummy = new DummyRepo();

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
}
