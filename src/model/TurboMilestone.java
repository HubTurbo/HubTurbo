package model;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone implements Listable {
	private int number;
	private String state;
	private String description;
	private Date dueOn;
	
	public TurboMilestone(Milestone milestone) {
		assert milestone != null;
		setTitle(milestone.getTitle());
		this.number = milestone.getNumber();
		this.state = milestone.getState();
		this.description = milestone.getDescription();
		this.dueOn = milestone.getDueOn();
	}
	
	public Milestone toGhMilestone() {
		Milestone ghMilestone = new Milestone();
		ghMilestone.setTitle(getTitle());
		ghMilestone.setNumber(number);
		ghMilestone.setState(state);
		ghMilestone.setDescription(description);
		ghMilestone.setDueOn(dueOn);
		return ghMilestone;
	}

	private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {return title.get();}
    public final void setTitle(String value) {title.set(value);}
    public StringProperty titleProperty() {return title;}

	public String getState() {
		return state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDueOn() {
		return dueOn;
	}

	public void setDueOn(Date dueOn) {
		this.dueOn = dueOn;
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
