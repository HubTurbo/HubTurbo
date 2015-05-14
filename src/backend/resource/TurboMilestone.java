package backend.resource;

import backend.resource.serialization.SerializableMilestone;
import org.eclipse.egit.github.core.Milestone;
import util.Utility;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class TurboMilestone {

	private static final String STATE_CLOSED = "closed";
	private static final String STATE_OPEN = "open";

	private void ______SERIALIZED_FIELDS______() {
	}

	private final int id;

	private String title;
	private LocalDate dueDate;
	private String description;
	private boolean isOpen;
	private int openIssues;
	private int closedIssues;

	private void ______TRANSIENT_FIELDS______() {
	}

	private void ______CONSTRUCTORS______() {
	}

	public TurboMilestone(int id, String title) {
		this.id = id;
		this.title = title;
		this.dueDate = LocalDate.now();
		this.description = "";
		this.isOpen = true;
		this.openIssues = 0;
		this.closedIssues = 0;
	}

	public TurboMilestone(Milestone milestone) {
		this.id = milestone.getNumber();
		this.title = milestone.getTitle();
		this.dueDate = Utility.dateToLocalDateTime(milestone.getDueOn()).toLocalDate();
		this.description = milestone.getDescription();
		this.isOpen = milestone.getState().equals(STATE_OPEN);
		this.openIssues = milestone.getOpenIssues();
		this.closedIssues = milestone.getClosedIssues();
	}

	public TurboMilestone(SerializableMilestone milestone) {
		this.id = milestone.id;
		this.title = milestone.title;
		this.dueDate = milestone.dueDate;
		this.description = milestone.description;
		this.isOpen = milestone.isOpen;
		this.openIssues = milestone.openIssues;
		this.closedIssues = milestone.closedIssues;
	}

	private void ______METHODS______() {
	}

	private void ______BOILERPLATE______() {
	}

	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public LocalDate getDueDate() {
		return dueDate;
	}
	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	public int getOpenIssues() {
		return openIssues;
	}
	public void setOpenIssues(int openIssues) {
		this.openIssues = openIssues;
	}
	public int getClosedIssues() {
		return closedIssues;
	}
	public void setClosedIssues(int closedIssues) {
		this.closedIssues = closedIssues;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TurboMilestone that = (TurboMilestone) o;

		if (closedIssues != that.closedIssues) return false;
		if (id != that.id) return false;
		if (isOpen != that.isOpen) return false;
		if (openIssues != that.openIssues) return false;
		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		if (dueDate != null ? !dueDate.equals(that.dueDate) : that.dueDate != null) return false;
		if (title != null ? !title.equals(that.title) : that.title != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (isOpen ? 1 : 0);
		result = 31 * result + openIssues;
		result = 31 * result + closedIssues;
		return result;
	}
}
