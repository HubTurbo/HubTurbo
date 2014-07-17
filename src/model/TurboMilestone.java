package model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone implements Listable {
	
	/*
	 * Attributes, Getters & Setters
	 */
	
	private int number;
	public int getNumber() {return number;}
	
	private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {return title.get();}
    public final void setTitle(String value) {title.set(value);}
    public StringProperty titleProperty() {return title;}
	
	private String state;
	public String getState() {return state;}
	
	private String description;
	public String getDescription() {return description;}
	public void setDescription(String description) {this.description = description;}
	
	private LocalDate dueOn;
	public LocalDate getDueOn() {return dueOn;}
	public void setDueOn(LocalDate dueOn) {this.dueOn = dueOn;}
	
	/*
	 * Constructors and Public Methods
	 */
	
	public TurboMilestone(){
		super();
	}
	
	public TurboMilestone(String title) {
		setTitle(title);
	}
	
	public TurboMilestone(Milestone milestone) {
		assert milestone != null;
		setTitle(milestone.getTitle());
		this.number = milestone.getNumber();
		this.state = milestone.getState();
		this.description = milestone.getDescription();
		this.dueOn = toLocalDate(milestone.getDueOn());
	}
	
	public Milestone toGhResource() {
		Milestone ghMilestone = new Milestone();
		ghMilestone.setTitle(getTitle());
		ghMilestone.setNumber(number);
		ghMilestone.setState(state);
		ghMilestone.setDescription(description);
		ghMilestone.setDueOn(toDate(dueOn));
		return ghMilestone;
	}
	
	public void copyValues(Object other){
		if(other.getClass() == TurboMilestone.class){
			TurboMilestone obj = (TurboMilestone)other;
			setTitle(obj.getTitle());
			this.state = obj.getState();
			this.description = obj.getDescription();
			this.dueOn = obj.getDueOn();
		}
	}
	
	/*
	 * Private Methods
	 */
	
	private LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		Instant instant = date.toInstant();
		ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
		LocalDate localDate = zdt.toLocalDate();
		// Minus one day as GitHub API milestone due date is one day
		// ahead of GitHub UI milestone due date
		return localDate.minusDays(1);
	}

	private Date toDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		// Plus one day as GitHub UI milestone due date is one day
		// behind of GitHub API milestone due date
		long epochInMilliseconds = (localDate.toEpochDay() + 1) * 24 * 60 * 60 * 1000;
		Date date = new Date(epochInMilliseconds);
		return date;
	}
	
	/*
	 * Overridden Methods
	 */

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
