package backend.json;

import backend.interfaces.RepoStore;
import backend.interfaces.StoreTask;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ReadTask extends StoreTask {

	private static final Logger logger = HTLog.get(ReadTask.class);

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
		Optional<String> input = RepoStore.read(repoId);

		if (!input.isPresent()) {
			logger.error("Unable to load " + repoId + " from JSON cache; defaulting to an empty Model");
			return new Model(repoId);
		} else {
			logger.info(HTLog.format(repoId, "Loaded from JSON cache"));
			SerializableModel sModel = new Gson().fromJson(input.get(),
				new TypeToken<SerializableModel>(){}.getType());
			return new Model(sModel);
		}
	}
}

