package backend.interfaces;

public abstract class CacheTask implements Runnable {
	public final String repoId;

	protected CacheTask(String repoId) {
		this.repoId = repoId;
	}

	public abstract void run();
}

