package backend.interfaces;

import util.Utility;

import java.io.File;
import java.util.Optional;

public abstract class StoreTask implements Runnable {
	public final String repoId;

	protected StoreTask(String repoId) {
		this.repoId = repoId;
	}

	public abstract void run();
}

