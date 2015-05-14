package backend.json;

import backend.resource.serialization.SerializableModel;
import backend.interfaces.CacheTask;
import backend.interfaces.RepoCache;
import com.google.gson.Gson;
import util.Utility;

class WriteTask extends CacheTask {

	public final SerializableModel toSave;

	public WriteTask(String repoName, SerializableModel toSave) {
		super(repoName);
		this.toSave = toSave;
	}

	@Override
	public void run() {
		save(repoName, toSave);
	}

	private void save(String repoName, SerializableModel model) {
		String output = new Gson().toJson(model);
		String newRepoName = RepoCache.escapeRepoName(repoName);
		Utility.writeFile(newRepoName, output);
	}
}

