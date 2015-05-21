package backend.interfaces;

import util.Utility;

import java.io.File;
import java.util.Optional;

public abstract class StoreTask implements Runnable {
	public final String repoId;

	protected StoreTask(String repoId) {
		this.repoId = repoId;

		ensureDirectoryExists();
	}

	public abstract void run();

	protected void write(String repoId, String output) {
		String newRepoName = RepoStore.escapeRepoName(repoId);
		Utility.writeFile(new File(RepoStore.DIRECTORY, newRepoName).getAbsolutePath(), output);
	}

	protected Optional<String> read(String repoId) {
		return Utility.readFile(
			new File(RepoStore.DIRECTORY, RepoStore.escapeRepoName(repoId)).getAbsolutePath());
	}

	private void ensureDirectoryExists() {
		File directory = new File(RepoStore.DIRECTORY);
		if (!directory.exists() || !directory.isDirectory()) {
			directory.mkdirs();
		}
	}
}

