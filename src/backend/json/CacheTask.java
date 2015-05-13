package backend.json;

public abstract class CacheTask {
	public final String repoName;

	protected CacheTask(String repoName) {
		this.repoName = repoName;
	}

	public abstract void update();
}

