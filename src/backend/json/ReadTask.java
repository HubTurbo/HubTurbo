package backend.json;

import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import backend.UpdateSignature;
import backend.interfaces.CacheTask;
import backend.interfaces.RepoCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.egit.github.core.RepositoryId;
import util.Utility;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ReadTask extends CacheTask {

	public final CompletableFuture<Model> response;

	public ReadTask(String repoName, CompletableFuture<Model> response) {
		super(repoName);
		this.response = response;
	}

	@Override
	public void run() {
		Model model = load(repoName);
		response.complete(model);
	}


	private Model load(String repoName) {
		Optional<String> input = Utility.readFile(RepoCache.escapeRepoName(repoName));
		if (!input.isPresent()) {
			return new Model(RepositoryId.createFromId(repoName), UpdateSignature.empty);
		} else {
			SerializableModel sModel = new Gson().fromJson(input.get(),
				new TypeToken<SerializableModel>(){}.getType());
			return new Model(sModel);
		}
	}
}

