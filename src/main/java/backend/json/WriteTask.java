package backend.json;

import backend.interfaces.RepoStore;
import backend.interfaces.StoreTask;
import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import util.HTLog;

class WriteTask extends StoreTask {

	private static final Logger logger = HTLog.get(WriteTask.class);

	public final SerializableModel toSave;

	public WriteTask(String repoName, SerializableModel toSave) {
		super(repoName);
		this.toSave = toSave;
	}

	@Override
	public void run() {
		save(repoId, toSave);
	}

	private void save(String repoId, SerializableModel model) {
		String output = new Gson().toJson(model);
		RepoStore.write(repoId, output);
		logger.info(HTLog.format(repoId, "Written to JSON store"));
	}
}

