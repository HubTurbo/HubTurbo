package logic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

import org.eclipse.egit.github.core.Issue;

public class TurboIssue {
//	private String title;
	private String description;
	private int id;
	private ArrayList<TurboLabel> labels;
	private TurboContributor assignee;
	private TurboMilestone milestone;
	
	public TurboIssue(String title, String desc) {
		setTitle(title);
		this.description = desc;
	}
	
	public TurboIssue(Issue issue) {
		setTitle(issue.getTitle());
		this.description = issue.getBody();
		this.id = issue.getNumber();
		this.assignee = new TurboContributor(issue.getAssignee());
		this.milestone = new TurboMilestone(issue.getMilestone());
	}
	
	@Override
	public String toString() {
		return "Issue " + getTitle();
	}
	
//	public String getTitle() {
//		return title;
//	}
//	public void setTitle(String title) {
//		this.title = title;
//	}
	
    private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {return title.get();}
    public final void setTitle(String value) {title.set(value);}
    public StringProperty titleProperty() {return title;}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<TurboLabel> getLabels() {
		return labels;
	}
	public void setLabels(ArrayList<TurboLabel> labels) {
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
