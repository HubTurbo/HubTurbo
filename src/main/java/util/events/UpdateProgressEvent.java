package util.events;

public class UpdateProgressEvent extends Event {
	public final String repoId;
	public final float progress;
	public final boolean done;

	public UpdateProgressEvent(String repoId, float progress) {
		assert progress >= 0 && progress <= 1;
		this.repoId = repoId;
		this.progress = progress;
		this.done = false;
	}

	public UpdateProgressEvent(String repoId) {
		this.repoId = repoId;
		this.progress = 1;
		this.done = true;
	}
}
