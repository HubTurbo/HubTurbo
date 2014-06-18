package logic;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone {
	private String title;
	
	public TurboMilestone(Milestone milestone) {
		this.title = milestone.getTitle();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}
	
	
}
