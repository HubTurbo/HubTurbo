package model;

import java.util.Iterator;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
	
	private List<Integer> parents;
	
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
		setTitle(other.getTitle());
		setDescription(other.getDescription());
		setOpen(other.getOpen());
		setId(other.getId());
		setLabels(FXCollections.observableArrayList(other.getLabels()));
		setAssignee(other.getAssignee());
		setMilestone(other.getMilestone());
	}
	
	public TurboIssue(Issue issue) {
		assert issue != null;
		
		setTitle(issue.getTitle());
		setDescription(issue.getBody());
		setOpen(new Boolean(issue.getState().equals("open")));
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
		ghIssue.setState(getOpen() ? "open" : "closed");
		if (assignee != null) ghIssue.setAssignee(assignee.toGhUser());
		if (milestone != null) ghIssue.setMilestone(milestone.toGhMilestone());
		ghIssue.setLabels(TurboLabel.toGhLabels(labels));
		
		String parentsMd = "*Parent(s): ";
		Iterator<Integer> parentsItr = parents.iterator();
		while (parentsItr.hasNext()) {
			parentsMd = parentsMd + "#" + parentsItr.next();
			if (parentsItr.hasNext()) {
				parentsMd = parentsMd + ", ";
			}
		}
		StringBuilder body = new StringBuilder();
		body.append(parentsMd + "\n");
		body.append("<hr>\n");
		body.append(getDescription());
		ghIssue.setBody(body.toString());
		
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
    
    private BooleanProperty state = new SimpleBooleanProperty();
    public final Boolean getOpen() {return state.get();}
    public final void setOpen(Boolean value) {state.set(value);}
    public BooleanProperty openProperty() {return state;}

    private IntegerProperty id = new SimpleIntegerProperty();
    public final int getId() {return id.get();}
    private final void setId(int value) {id.set(value);}
    public IntegerProperty idProperty() {return id;}
    
	public ObservableList<TurboLabel> getLabels() {
		return labels;
	}
	public void setLabels(ObservableList<TurboLabel> labels) {
		if (this.labels == null) {
			this.labels = labels;
		} else if (labels != this.labels) {
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
	
	public List<Integer> getParents() {
		return parents;
	}

	public void addParent(int issueId) {
		parents.add(new Integer(issueId));
	}
	
	public void removeParent(int issueId) {
		parents.remove(new Integer(issueId));
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
