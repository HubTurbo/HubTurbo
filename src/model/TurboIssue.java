package model;

import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

public class TurboIssue implements Listable {
	private ObservableList<TurboLabel> labels;
	private TurboCollaborator assignee;
	private TurboMilestone milestone;
	
	public TurboIssue(String title, String desc) {
		assert title != null;
		assert desc != null;
		
		setTitle(title);
		setDescription(desc);
		labels = FXCollections.observableArrayList();
//		TurboLabel tl = new TurboLabel();
//		tl.setName("feature");
//		labels.add(tl);
	}
	
	// Copy constructor
	public TurboIssue(TurboIssue other) {
		other.cloneInto(this);
	}
	
	public TurboIssue cloneInto(TurboIssue other) {
		setTitle(other.getTitle());
		setDescription(other.getDescription());
		setId(other.getId());
		setLabels(FXCollections.observableArrayList(other.getLabels()));
		setAssignee(other.getAssignee());
		setMilestone(other.getMilestone());
		return other;
	}
	
	public TurboIssue(Issue issue) {
		assert issue != null;
		
		setTitle(issue.getTitle());
		setDescription(issue.getBody());
		setId(issue.getNumber());
		
		this.assignee = issue.getAssignee() == null ? null : new TurboCollaborator(issue.getAssignee());
		this.milestone = issue.getMilestone() == null ? null : new TurboMilestone(issue.getMilestone());
		this.labels = translateLabels(issue);
	}
	
	private ObservableList<TurboLabel> translateLabels(Issue issue) {
		ObservableList<TurboLabel> turboLabels = FXCollections.observableArrayList();
		
		if (issue.getLabels() == null) return turboLabels;
		
		List<Label> labels = issue.getLabels();
		for (Label label : labels) {
			turboLabels.add(new TurboLabel(label));
		}
		return turboLabels;
	}
	
	public Issue toGhIssue() {
		Issue ghIssue = new Issue();
		ghIssue.setTitle(getTitle());
		ghIssue.setBody(getDescription());
		if (assignee != null) ghIssue.setAssignee(assignee.toGhUser());
		if (milestone != null) ghIssue.setMilestone(milestone.toGhMilestone());
		ghIssue.setLabels(TurboLabel.toGhLabels(labels));
		return ghIssue;
	}
	
	@Override
	public String toString() {
		return "Issue " + getTitle();
	}

	private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {return title.get();}
    public final void setTitle(String value) {title.set(value);}
    public StringProperty titleProperty() {return title;}
	
    private StringProperty description = new SimpleStringProperty();
    public final String getDescription() {return description.get();}
    public final void setDescription(String value) {description.set(value);}
    public StringProperty descriptionProperty() {return description;}

    private IntegerProperty id = new SimpleIntegerProperty();
    public final int getId() {return id.get();}
    private final void setId(int value) {id.set(value);}
    public IntegerProperty idProperty() {return id;}
    
	public ObservableList<TurboLabel> getLabels() {
		return labels;
	}
	public void setLabels(ObservableList<TurboLabel> labels) {
		if (labels != this.labels) {
			this.labels.clear();
			this.labels.addAll(labels);
		}
	}
	public TurboCollaborator getAssignee() {
		return assignee;
	}
	public void setAssignee(TurboCollaborator assignee) {
		this.assignee = assignee;
	}
	public TurboMilestone getMilestone() {
		return milestone;
	}
	public void setMilestone(TurboMilestone milestone) {
		this.milestone = milestone;
	}

	@Override
	public String getListName() {
		return "#" + getId() + " " + getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		TurboIssue other = (TurboIssue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (getId() != other.getId())
			return false;
		return true;
	}
	

}
