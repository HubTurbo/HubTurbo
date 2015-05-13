package backend.interfaces;

public abstract class CacheTask implements Runnable {
	public final String repoName;

	protected CacheTask(String repoName) {
		this.repoName = repoName;
	}

	public abstract void run();
}

