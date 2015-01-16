package storage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.eclipse.egit.github.core.PullRequest;

class TurboSerializableIssue {
	private String creator;
	private String createdAt;
	private LocalDateTime updatedAt;
	private int numOfComments;
	private PullRequest pullRequest;
	
	private int id;
	private String title;
	private String description;
	
	// for comments, but not used for current version
	//private String cachedDescriptionMarkup;

	private int parentIssue;
	private boolean state;
	
	private TurboUser assignee;
	private TurboSerializableMilestone milestone;
	private String htmlUrl;
	private List<TurboSerializableLabel> labels;
	
	public TurboSerializableIssue(TurboIssue issue) {
		this.creator = issue.getCreator();
		this.createdAt = issue.getCreatedAt();
		this.updatedAt = issue.getUpdatedAt();
		this.numOfComments = issue.getNumOfComments();
		this.pullRequest = issue.getPullRequest();
		
		this.id = issue.getId();
		this.title = issue.getTitle();
		this.description = issue.getDescription();
		//this.cachedDescriptionMarkup = issue.getDescriptionMarkup();
		
		this.parentIssue = issue.getParentIssue();
		this.state = issue.isOpen();
		this.assignee = issue.getAssignee();
		
		TurboMilestone turboMilestone = issue.getMilestone();
		if (turboMilestone != null) {
			this.milestone = new TurboSerializableMilestone(issue.getMilestone());
		} else {
			this.milestone = null;
		}
		
		this.htmlUrl = issue.getHtmlUrl();
		
		this.labels = convertFromListOfTurboLabels(issue.getLabels());
	}
	
	private List<TurboSerializableLabel> convertFromListOfTurboLabels(List<TurboLabel> turboLabelsList) {
		List<TurboSerializableLabel> list = new ArrayList<TurboSerializableLabel>();
		if (turboLabelsList == null) {
			return null;
		} else {
			for (TurboLabel label : turboLabelsList) {
				list.add(new TurboSerializableLabel(label));
			}
		}
		return list;
	}
	
	public TurboIssue toTurboIssue(Model model) {
		TurboIssue tI = new TurboIssue(this.title, this.description, model);
		
		tI.setCreator(creator);
		tI.setCreatedAt(createdAt);
		tI.setUpdatedAt(updatedAt);
		tI.setNumOfComments(numOfComments);
		tI.setPullRequest(pullRequest);
		
		tI.setId(id);
		//tI.setDescriptionMarkup(cachedDescriptionMarkup);
		
		tI.setParentIssue(parentIssue);
		tI.setOpen(state);
		tI.setAssignee(assignee);
		if (milestone == null) {
			tI.setMilestone(null);
		} else {
			tI.setMilestone(milestone.toTurboMilestone());
		}
			
		tI.setHtmlUrl(htmlUrl);

		ObservableList<TurboLabel> turboLabelList = FXCollections.observableArrayList();
		if (labels == null) {
			tI.setLabels(turboLabelList);
		} else {
			for (TurboSerializableLabel label : labels) {
				turboLabelList.add(label.toTurboLabel());
			}
			tI.setLabels(turboLabelList);
		}
		return tI;
	}
}
