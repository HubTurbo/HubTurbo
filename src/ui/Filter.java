package ui;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import logic.TurboIssue;

public class Filter {
	
	private ArrayList<String> titles = new ArrayList<>();
	private ArrayList<String> exceptTitles = new ArrayList<>();
	private ArrayList<String> milestones = new ArrayList<String>();
	private ArrayList<String> exceptMilestones = new ArrayList<String>();
	private Filter disjunct = null;
	
	public Filter withTitle(String title) {
		titles.add(title);
		return this;
	}
	
	public Filter exceptWithTitle(String title) {
		exceptTitles.add(title);
		return this;
	}

	public Filter underMilestone(String milestoneName) {
		milestones.add(milestoneName);
		return this;
	}
	
	public Filter exceptUnderMilestone(String milestoneName) {
		exceptMilestones.add(milestoneName);
		return this;
	}
	
	public Filter or(Filter disjunct) {
		this.disjunct = disjunct;
		return disjunct;
	}
	
	public Filter or() {
		return this.or(new Filter());
	}
	
	public boolean isEmpty() {
		return titles.isEmpty() && exceptTitles.isEmpty() && milestones.isEmpty() && exceptMilestones.isEmpty()
				&& disjunct == null;
	}
	
	public boolean isSatisfiedBy(TurboIssue issue) {
		if (isEmpty()) return true;
		
		boolean containsTitle = false;
		for (String title : titles) {
			if (issue.getTitle().contains(title)) {
				containsTitle = true;
				break;
			}
		}
		
		return containsTitle;
	}
	
	public ObservableList<TurboIssue> returnFiltered (ObservableList<TurboIssue> issues) {
		return FXCollections.observableArrayList(issues.stream().filter((issue) -> {
			return isSatisfiedBy(issue);
		}).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return "Filter [titles=" + titles + ", exceptTitles=" + exceptTitles
				+ ", milestones=" + milestones + ", exceptMilestones="
				+ exceptMilestones + ", disjunct=" + disjunct + "]";
	}

//	public String toReadableString() {
//		return "filtered";
//	}

}
