package backend.json;

import backend.resource.serialization.SerializableModel;
import backend.interfaces.CacheTask;
import backend.interfaces.RepoStore;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Utility;

class WriteTask extends CacheTask {

	private static final Logger logger = LogManager.getLogger(WriteTask.class.getName());

	public final SerializableModel toSave;

	public WriteTask(String repoName, SerializableModel toSave) {
		super(repoName);
		this.toSave = toSave;
	}

	@Override
	public void run() {
		save(repoId, toSave);
	}

	private void save(String repoName, SerializableModel model) {
		String output = new Gson().toJson(model);
		String newRepoName = RepoStore.escapeRepoName(repoName);
		Utility.writeFile(newRepoName, output);
		logger.info("Written " + repoName + " to JSON store");
	}
}

