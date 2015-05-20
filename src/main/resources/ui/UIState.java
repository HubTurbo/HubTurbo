package ui;

/**
 * A thread-safe class for aggregating application-level state.
 * The UI should contain only application-level operations.
 */
public class UIState {
	private String primaryRepo;

	public UIState(String primaryRepo) {
		this.primaryRepo = primaryRepo;
	}

	public synchronized String getPrimaryRepo() {
		return primaryRepo;
	}

	public synchronized void setPrimaryRepo(String repoId) {
		primaryRepo = repoId;
	}
}
