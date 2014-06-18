package logic;

import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

public class TurboIssue {
	private Issue ghIssue;
	private ObservableList<TurboLabel> labels;
	private TurboContributor assignee;
	private TurboMilestone milestone;
	
	public TurboIssue(String title, String desc) {
		setTitle(title);
		setDescription(desc);
	}
	
	public TurboIssue(Issue issue) {
		this.ghIssue = issue;
		
		setTitle(issue.getTitle());
		setDescription(issue.getBody());
		setId(issue.getNumber());
		
		this.assignee = new TurboContributor(issue.getAssignee());
		this.milestone = new TurboMilestone(issue.getMilestone());
		this.labels = getLabels(issue);
	}
	
	private ObservableList<TurboLabel> getLabels(Issue issue) {
		ObservableList<TurboLabel> turboLabels = FXCollections.observableArrayList();
		List<Label> labels = issue.getLabels();
		for (Label label : labels) {
			turboLabels.add(new TurboLabel(label));
		}
		return turboLabels;
	}

	@Override
	public String toString() {
		return "Issue " + getTitle();
	}
	
	public Issue getGhIssue() {
		return ghIssue;
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
		this.labels = labels;
	}
	public TurboContributor getAssignee() {
		return assignee;
	}
	public void setAssignee(TurboContributor assignee) {
		this.assignee = assignee;
	}
	public TurboMilestone getMilestone() {
		return milestone;
	}
	public void setMilestone(TurboMilestone milestone) {
		this.milestone = milestone;
	}
}
