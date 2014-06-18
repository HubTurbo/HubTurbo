package logic;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

public class TurboIssue {
	private Issue ghIssue;
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
		this.ghIssue = issue;
		setTitle(issue.getTitle());
		this.description = issue.getBody();
		this.id = issue.getNumber();
		this.assignee = new TurboContributor(issue.getAssignee());
		this.milestone = new TurboMilestone(issue.getMilestone());
		this.labels = getLabels(issue);
	}
	
	private ArrayList<TurboLabel> getLabels(Issue issue) {
		ArrayList<TurboLabel> turboLabels = new ArrayList<TurboLabel>();
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
