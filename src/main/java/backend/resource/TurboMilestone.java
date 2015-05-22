package backend.resource;

import backend.resource.serialization.SerializableMilestone;
import org.eclipse.egit.github.core.Milestone;
import util.Utility;

import java.time.LocalDate;
import java.util.Optional;

@SuppressWarnings("unused")
public class TurboMilestone {

	private static final String STATE_CLOSED = "closed";
	private static final String STATE_OPEN = "open";

	private void ______SERIALIZED_FIELDS______() {
	}

	private final int id;

	private String title;
	private Optional<LocalDate> dueDate;
	private String description;
	private boolean isOpen;
	private int openIssues;
	private int closedIssues;

	private void ______TRANSIENT_FIELDS______() {
	}

	private final String repoId;

	private void ______CONSTRUCTORS______() {
	}

	public TurboMilestone(String repoId, int id, String title) {
		this.id = id;
		this.title = title;
		this.dueDate = Optional.empty();
		this.description = "";
		this.isOpen = true;
		this.openIssues = 0;
		this.closedIssues = 0;
		this.repoId = repoId;
	}

	public TurboMilestone(String repoId, Milestone milestone) {
		this.id = milestone.getNumber();
		this.title = milestone.getTitle();
		this.dueDate = milestone.getDueOn() == null
			? Optional.empty()
			: Optional.of(Utility.dateToLocalDateTime(milestone.getDueOn()).toLocalDate());
		this.description = milestone.getDescription() == null ? "" : milestone.getDescription();
		this.isOpen = milestone.getState().equals(STATE_OPEN);
		this.openIssues = milestone.getOpenIssues();
		this.closedIssues = milestone.getClosedIssues();
		this.repoId = repoId;
	}

	public TurboMilestone(String repoId, SerializableMilestone milestone) {
		this.id = milestone.getId();
		this.title = milestone.getTitle();
		this.dueDate = milestone.getDueDate();
		this.description = milestone.getDescription();
		this.isOpen = milestone.isOpen();
		this.openIssues = milestone.getOpenIssues();
		this.closedIssues = milestone.getClosedIssues();
		this.repoId = repoId;
	}

	private void ______METHODS______() {
	}

	@Override
	public String toString() {
		return title;
	}

	private void ______BOILERPLATE______() {
	}

	public String getRepoId() {
		return repoId;
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
	public Optional<LocalDate> getDueDate() {
		return dueDate;
	}
	public void setDueDate(Optional<LocalDate> dueDate) {
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
		if (!description.equals(that.description)) return false;
		if (!dueDate.equals(that.dueDate)) return false;
		if (!title.equals(that.title)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + title.hashCode();
		result = 31 * result + dueDate.hashCode();
		result = 31 * result + description.hashCode();
		result = 31 * result + (isOpen ? 1 : 0);
		result = 31 * result + openIssues;
		result = 31 * result + closedIssues;
		return result;
	}
}
