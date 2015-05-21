package backend.json;

import backend.interfaces.StoreTask;
import backend.interfaces.RepoStore;
import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import util.HTLog;
import util.Utility;

import java.io.File;

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

