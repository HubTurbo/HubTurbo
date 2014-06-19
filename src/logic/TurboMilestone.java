package logic;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone implements Listable {
	private Milestone ghMilestone;
	private String title;
	
	public TurboMilestone(Milestone milestone) {
		this.ghMilestone = milestone;
		if (milestone != null) {
			this.title = milestone.getTitle();
		}
	}
	
	public Milestone getGhMilestone() {
		return ghMilestone;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	@Override
	public String getListName() {
		return getTitle();
	}
	
	
}
