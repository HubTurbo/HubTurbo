package model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone implements Listable {
	
	private static final String CUSTOM_DATETIME_PATTERN = "d MMMM yyyy";
	/*
	 * Attributes, Getters & Setters
	 */
	
	private int number = -1;
	public int getNumber() {return number;}
	public void setNumber(int number) {
		this.number = number;
	}
	
	private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {return title.get();}
    public final void setTitle(String value) {title.set(value);}
    public StringProperty titleProperty() {return title;}
	
	private String state;
	public String getState() {return state;}
	public void setState(String state) {
		this.state = state;
	}
	
	private String description;
	public String getDescription() {return description;}
	public void setDescription(String description) {this.description = description;}
	
	private LocalDate dueOn;
	public LocalDate getDueOn() {return dueOn;}
	public void setDueOn(LocalDate dueOn) {
		this.dueOn = dueOn;
		if (this.dueOn != null) {
			setDueOnString(getDueOn().format(DateTimeFormatter.ofPattern(CUSTOM_DATETIME_PATTERN)));
		}
	}
	public void setDueOn(String dueOnString) {
		if (dueOnString == null) {
			this.dueOn = null;
		} else {
			this.dueOn = LocalDate.parse(dueOnString, DateTimeFormatter.ofPattern(CUSTOM_DATETIME_PATTERN));
		}
	}

	private StringProperty dueOnString = new SimpleStringProperty();
    public final String getDueOnString() {return dueOnString.get();}
    public final void setDueOnString(String value) {dueOnString.set(value);}
    public StringProperty dueOnStringProperty() {return dueOnString;}
	
	private IntegerProperty closed = new SimpleIntegerProperty();
    public final Integer getClosed() {return closed.get();}
    public final void setClosed(Integer value) {closed.set(value);}
    public IntegerProperty closedProperty() {return closed;}
    
    private IntegerProperty open = new SimpleIntegerProperty();
    public final Integer getOpen() {return open.get();}
    public final void setOpen(Integer value) {open.set(value);}
    public IntegerProperty openProperty() {return open;}
	
	/*
	 * Constructors and Public Methods
	 */
	
	public TurboMilestone() {
		setTitle("");
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
		setDueOn(toLocalDate(milestone.getDueOn()));
		setClosed(milestone.getClosedIssues());
		setOpen(milestone.getOpenIssues());
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
			setDueOn(obj.getDueOn());
			setClosed(obj.getClosed());
			setOpen(obj.getOpen());
		}
	}
	
	public double getProgress(){
		if (getClosed() == 0 && getOpen() == 0) {
			return 0;
		}
		double total = getClosed() + getOpen();
		double progress = getClosed() / total;
		return progress;
	}
	
	public Long relativeDueDateInDays() {
		if (getDueOn() == null) {
			return null;
		}
		long daysUntilDueDate = LocalDate.now().until(getDueOn(), ChronoUnit.DAYS);
		return daysUntilDueDate;
	}
	
	public String relativeDueDateInString() {
		Long days = relativeDueDateInDays();
		if (days == null) {return null;}
		if (days < 0) {return "over";}
		if (days == 0) {return "today";}
		if (days > 0) {return days.toString() + " days";}
		
		return ""; //stub value, should never be returned
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
