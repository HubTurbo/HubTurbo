package storage;

import model.*;
import org.eclipse.egit.github.core.PullRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class SerializableIssue {
	private String creator;
	private String createdAt;
	private LocalDateTime updatedAt;
	private int numOfComments;
	private PullRequest pullRequest;
	
	private int id;
	private String title;
	private String description;
	
	private int parentIssue;
	private boolean state;
	
	private TurboUser assignee;
	private SerializableMilestone milestone;
	private String htmlUrl;
	private List<SerializableLabel> labels;
	
	public SerializableIssue(TurboIssue issue) {
		this.creator = issue.getCreator();
		this.createdAt = issue.getCreatedAt();
		this.updatedAt = issue.getUpdatedAt();
		this.numOfComments = issue.getCommentCount();
		this.pullRequest = issue.getPullRequest();
		
		this.id = issue.getId();
		this.title = issue.getTitle();
		this.description = issue.getDescription();

		this.parentIssue = issue.getParentIssue();
		this.state = issue.isOpen();
		this.assignee = issue.getAssignee();
		
		TurboMilestone turboMilestone = issue.getMilestone();
		if (turboMilestone != null) {
			this.milestone = new SerializableMilestone(issue.getMilestone());
		} else {
			this.milestone = null;
		}
		
		this.htmlUrl = issue.getHtmlUrl();
		
		List<TurboLabel> turboLabelObservableList = issue.getLabels();
		List<TurboLabel> turboLabelList = turboLabelObservableList.stream().collect(Collectors.toList());
		this.labels = convertFromListOfTurboLabels(turboLabelList);
	}
	
	private List<SerializableLabel> convertFromListOfTurboLabels(List<TurboLabel> turboLabelsList) {
		List<SerializableLabel> list = new ArrayList<SerializableLabel>();
		if (turboLabelsList == null) {
			return null;
		} else {
			for (TurboLabel label : turboLabelsList) {
				list.add(new SerializableLabel(label));
			}
		}
		return list;
	}
	
	public TurboIssue toTurboIssue(Model model) {
		TurboIssue tI = new TurboIssue(this.title, this.description, model);
		
		tI.setCreator(creator);
		tI.setCreatedAt(createdAt);
		tI.setUpdatedAt(updatedAt);
		tI.setCommentCount(numOfComments);
		tI.setPullRequest(pullRequest);
		
		tI.setId(id);

		tI.setParentIssue(parentIssue);
		tI.setOpen(state);
		tI.setAssignee(assignee);
		tI.setHtmlUrl(htmlUrl);

		if (milestone == null) {
			tI.setTemporaryMilestone(Optional.empty());
		} else {
			tI.setTemporaryMilestone(Optional.of(milestone.toTurboMilestone().toGhResource()));
		}

		if (labels == null) {
			tI.setTemporaryLabels(Optional.empty());
		} else {
			tI.setTemporaryLabels(Optional.of(labels.stream()
				.map(SerializableLabel::toTurboLabel)
				.map(TurboLabel::toGhResource)
				.collect(Collectors.toList())));
		}

		return tI;
	}
}
