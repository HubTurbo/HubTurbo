package model;

import java.util.Date;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone implements Listable {
	private String title;
	private int number;
	private String state;
	private String description;
	private Date dueOn;
	
	public TurboMilestone(Milestone milestone) {
		assert milestone != null;
		this.title = milestone.getTitle();
		this.number = milestone.getNumber();
		this.state = milestone.getState();
		this.description = milestone.getDescription();
		this.dueOn = milestone.getDueOn();
	}
	
	public Milestone toGhMilestone() {
		Milestone ghMilestone = new Milestone();
		ghMilestone.setTitle(title);
		ghMilestone.setNumber(number);
		ghMilestone.setState(state);
		ghMilestone.setDescription(description);
		ghMilestone.setDueOn(dueOn);
		return ghMilestone;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}
	
	public int getNumber() {
		return number;
	}
	
	@Override
	public String getListName() {
		return getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TurboMilestone other = (TurboMilestone) obj;
		if (number != other.number)
			return false;
		return true;
	}
	
}
