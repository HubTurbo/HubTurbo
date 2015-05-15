package backend.json;

import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import backend.UpdateSignature;
import backend.interfaces.CacheTask;
import backend.interfaces.RepoStore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import util.Utility;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ReadTask extends CacheTask {

	private static final Logger logger = LogManager.getLogger(ReadTask.class.getName());

	public final CompletableFuture<Model> response;

	public ReadTask(String repoId, CompletableFuture<Model> response) {
		super(repoId);
		this.response = response;
	}

	@Override
	public void run() {
		Model model = load(repoId);
		response.complete(model);
	}


	private Model load(String repoId) {
		Optional<String> input = Utility.readFile(RepoStore.escapeRepoName(repoId));
		if (!input.isPresent()) {
			logger.error("Unable to load " + repoId + " from JSON cache; defaulting to an empty Model");
			return new Model(RepositoryId.createFromId(repoId), UpdateSignature.empty);
		} else {
			logger.info("Loaded " + repoId + " from JSON cache");
			SerializableModel sModel = new Gson().fromJson(input.get(),
				new TypeToken<SerializableModel>(){}.getType());
			return new Model(sModel);
		}
	}
}

