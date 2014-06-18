package logic;

import java.util.ArrayList;

public class Issue {
	private String title;
	private String description;
	private int id;
	private ArrayList<Label> labels;
	private Contributor assignee;
	private Milestone milestone;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
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
	public ArrayList<Label> getLabels() {
		return labels;
	}
	public void setLabels(ArrayList<Label> labels) {
		this.labels = labels;
	}
	public Contributor getAssignee() {
		return assignee;
	}
	public void setAssignee(Contributor assignee) {
		this.assignee = assignee;
	}
	public Milestone getMilestone() {
		return milestone;
	}
	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}
}
