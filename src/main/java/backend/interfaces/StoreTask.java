package backend.interfaces;

public abstract class StoreTask implements Runnable {
	public final String repoId;

	protected StoreTask(String repoId) {
		this.repoId = repoId;
	}

	public abstract void run();
}

