package backend;

import java.time.LocalDate;

public class SerializableMilestone {
	public final int id;
	public final String title;
	public final LocalDate dueDate;
	public final String description;
	public final boolean isOpen;
	public final int openIssues;
	public final int closedIssues;

	public SerializableMilestone(TurboMilestone milestone) {
		this.id = milestone.getId();
		this.title = milestone.getTitle();
		this.dueDate = milestone.getDueDate();
		this.description = milestone.getDescription();
		this.isOpen = milestone.isOpen();
		this.openIssues = milestone.getOpenIssues();
		this.closedIssues = milestone.getClosedIssues();
	}
}
