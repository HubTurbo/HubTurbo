package model;

import java.util.HashMap;
import java.util.HashSet;
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
	
	private static final String STATE_CLOSED = "closed";
	private static final String STATE_OPEN = "open";
	private static final String REGEX_REPLACE_DESC = "^[^<>]*<hr>";
	private static final String REGEX_SPLIT_PARENT = "(,\\s+)?#";
	private static final String REGEX_SPLIT_LINES = "(\\r?\\n)+";
	private static final String METADATA_HEADER_PARENT = "* Parent(s): ";
	private static final String METADATA_SEPERATOR = "<hr>";
	public static final String REMOVED_TAG = "removed";
	public static final String ADDED_TAG = "added";
	
	/*
	 * Attributes, Getters & Setters
	 */
	
	private IntegerProperty id = new SimpleIntegerProperty();
    public final int getId() {
    	return id.get();
    }
    public final void setId(int value) {
    	id.set(value);
    }
    public IntegerProperty idProperty() {
    	return id;
    }
	
	private StringProperty title = new SimpleStringProperty();
    public final String getTitle() {
    	return title.get();
    }
    public final void setTitle(String value) {
    	title.set(value);
    }
    public StringProperty titleProperty() {
    	return title;
    }
	
    private StringProperty description = new SimpleStringProperty();
    public final String getDescription() {
    	return description.get();
    }
    public final void setDescription(String value) {
    	description.set(value);
    }
    public StringProperty descriptionProperty() {
    	return description;
    }
    
    private BooleanProperty state = new SimpleBooleanProperty();
    public final Boolean getOpen() {
    	return state.get();
    }
    public final void setOpen(Boolean value) {
    	state.set(value);
    }
    public BooleanProperty openProperty() {
    	return state;
    }
    
    private TurboUser assignee;
    public TurboUser getAssignee() {
    	return assignee;
    }
	public void setAssignee(TurboUser assignee) {
		this.assignee = assignee;
	}
	
	private TurboMilestone milestone;
	public TurboMilestone getMilestone() {
		return milestone;
	}
	public void setMilestone(TurboMilestone milestone) {
		this.milestone = milestone;
	}
	
	private String htmlUrl;
    public String getHtmlUrl() {
    	return htmlUrl;
    }
	private void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}
	
	private ObservableList<TurboLabel> labels;
	public ObservableList<TurboLabel> getLabels() {return labels;}
	public void setLabels(ObservableList<TurboLabel> labels) {
		if (this.labels == null) {
			this.labels = labels;
		} else if (labels != this.labels) {
			this.setOpen(true);
			for (TurboLabel currentLabel : labels){
				if (currentLabel.getName().equalsIgnoreCase("wontfix") || 
					 currentLabel.getName().equalsIgnoreCase("duplicate") ||
					 currentLabel.getName().equalsIgnoreCase("invalid") ) {
					this.setOpen(false);
					break;
				}
			}
			this.labels.clear();
			this.labels.addAll(labels);
		}	
	}
	
	private ObservableList<Integer> parents;
	public ObservableList<Integer> getParents() {return parents;}
	public void setParents(ObservableList<Integer> parentNumbers) {
		if (this.parents == null) {
			this.parents = parentNumbers;
		} else if (parentNumbers != this.parents) {
			this.parents.clear();
			this.parents.addAll(parentNumbers);
		}
	}

	/*
	 * Constructors & Public Methods
	 */
	
	public TurboIssue(String title, String desc) {
		assert title != null;
		assert desc != null;
		
		setTitle(title);
		setDescription(desc);
		labels = FXCollections.observableArrayList();
		parents = FXCollections.observableArrayList();
		setOpen(true);
	}
	
	// Copy constructor
	public TurboIssue(TurboIssue other) {
		assert other != null;
		
		copyValues(other);	
	}
	
	public TurboIssue(Issue issue) {
		assert issue != null;
		
		setHtmlUrl(issue.getHtmlUrl());
		setTitle(issue.getTitle());
		setOpen(new Boolean(issue.getState().equals(STATE_OPEN)));
		setId(issue.getNumber());
		setDescription(extractDescription(issue.getBody()));
		setAssignee(issue.getAssignee() == null ? null : new TurboUser(issue.getAssignee()));
		setMilestone(issue.getMilestone() == null ? null : new TurboMilestone(issue.getMilestone()));
		setLabels(translateLabels(issue.getLabels()));
		setParents(extractParentNumbers(issue.getBody()));
	}

	public Issue toGhResource() {
		Issue ghIssue = new Issue();
		ghIssue.setNumber(getId());
		ghIssue.setTitle(getTitle());
		ghIssue.setState(getOpen() ? STATE_OPEN : STATE_CLOSED);
		if (assignee != null) ghIssue.setAssignee(assignee.toGhResource());
		if (milestone != null) ghIssue.setMilestone(milestone.toGhResource());
		ghIssue.setLabels(TurboLabel.toGhLabels(labels));
		ghIssue.setBody(buildBody());
		return ghIssue;
	}
	
	public void copyValues(TurboIssue other) {
		assert other != null;
		
		setHtmlUrl(other.getHtmlUrl());
		setTitle(other.getTitle());
		setOpen(other.getOpen());
		setId(other.getId());
		setDescription(other.getDescription());
		setAssignee(other.getAssignee());
		setMilestone(other.getMilestone());
		setLabels(FXCollections.observableArrayList(other.getLabels()));
		setParents(FXCollections.observableArrayList(other.getParents()));
		setParents(other.getParents());
	}
	
	/**
	 * Modifies @param latest to contain the merged changes of this TurboIssue object and @param latest wrt @param edited
	 * Stores change log in @param changeLog
	 * @return true if issue description has been successfully merged, false otherwise
	 * */
	protected boolean mergeIssues(TurboIssue original, TurboIssue latest, StringBuilder changeLog){
		mergeTitle(original, latest, changeLog);
		boolean fullMerge = mergeDescription(original, latest, changeLog);		
		mergeParents(original, latest, changeLog);
		mergeLabels(original, latest, changeLog);
		mergeAssignee(original, latest, changeLog);
		mergeMilestone(original, latest, changeLog);
		mergeOpen(original, latest);
		return fullMerge;
	}
	
	private void mergeLabels(TurboIssue original, TurboIssue latest, StringBuilder changeLog) {
		ObservableList<TurboLabel> originalLabels = original.getLabels();
		ObservableList<TurboLabel> editedLabels = this.getLabels();
		HashMap<String, HashSet<TurboLabel>> changeSet = getChangesToList(originalLabels, editedLabels);
		ObservableList<TurboLabel> latestLabels = latest.getLabels();
		HashSet<TurboLabel> removed = changeSet.get(REMOVED_TAG);
		HashSet<TurboLabel> added = changeSet.get(ADDED_TAG);
		
		latestLabels.removeAll(removed);
	
		for(TurboLabel label: added){
			if(!latestLabels.contains(label)){
				latestLabels.add(label);
			}
		}
		logLabelChange(removed, added, changeLog);
		latest.setLabels(latestLabels);
	}

	/**
	 * Gets the changes made to the a list of items
	 * @return HashMap the a list of items removed from the original list
	 * 			and a list of items added to the original list
	 * */
	protected <T> HashMap<String, HashSet<T>> getChangesToList(List<T> original, List<T> edited){
		HashMap<String, HashSet<T>> changeSet = new HashMap<String, HashSet<T>>();
		HashSet<T> removed = new HashSet<T>(original);
		HashSet<T> added = new HashSet<T>(edited);
		removed.removeAll(edited);
		added.removeAll(original);
		
		changeSet.put(REMOVED_TAG, removed);
		changeSet.put(ADDED_TAG, added);
		
		return changeSet;
	}
	
	private void logLabelChange(HashSet<TurboLabel> removed, HashSet<TurboLabel> added, StringBuilder changeLog){
		if(added.size() > 0){
			changeLog.append("Added labels: " + added.toString() + "\n");
		}
		if(removed.size() > 0){
			changeLog.append("Removed labels: " + removed.toString() + "\n");
		}
	}
	
	private void mergeMilestone(TurboIssue original, TurboIssue latest, StringBuilder changeLog) {
		TurboMilestone originalMilestone = original.getMilestone();
		TurboMilestone editedMilestone = this.getMilestone();
		int originalMNumber = (originalMilestone != null) ? originalMilestone.getNumber() : 0;
		int editedMNumber = (editedMilestone != null) ? editedMilestone.getNumber() : 0;
		if (editedMNumber != originalMNumber) {
			// this check is for cleared milestone
			if (editedMilestone == null) {
				editedMilestone = new TurboMilestone();
			}
			latest.setMilestone(editedMilestone);
			logMilestoneChange(editedMilestone, changeLog);
		}
	}

	private void logMilestoneChange(TurboMilestone editedMilestone, StringBuilder changeLog){
		changeLog.append("Changed milestone to: "+ editedMilestone.getTitle() + "\n");
	}
	
	private void mergeOpen(TurboIssue original, TurboIssue latest) {
		Boolean originalState = original.getOpen();
		Boolean editedState = this.getOpen();
		if (!editedState.equals(originalState)) {
			latest.setOpen(editedState);
		}
	}

	private void mergeAssignee(TurboIssue original, TurboIssue latest, StringBuilder changeLog) {
		TurboUser originalAssignee = original.getAssignee();
		TurboUser editedAssignee = this.getAssignee();
		// this check is for cleared assignee
		if(originalAssignee == null){
			originalAssignee = new TurboUser();
		}
		if (editedAssignee == null) {
			editedAssignee = new TurboUser();
		} 
		if (!originalAssignee.equals(editedAssignee)) {
			latest.setAssignee(editedAssignee);
			logAssigneeChange(editedAssignee, changeLog);
		}
	}
	
	private void logAssigneeChange(TurboUser assignee, StringBuilder changeLog){
		changeLog.append("Changed issue assignee to: "+ assignee.getGithubName() + "\n");
	}

	/**
	 * Merges changes to description only if the description in the latest version has not been updated. 
	 * Returns false if description was not merged because the issue's description has been modified in @param latest
	 * */
	private boolean mergeDescription(TurboIssue original, TurboIssue latest, StringBuilder changeLog) {
		String originalDesc = original.getDescription();
		String editedDesc = this.getDescription();
		String latestDesc = latest.getDescription();
		if (!editedDesc.equals(originalDesc)) {
			if(!latestDesc.equals(originalDesc)){
				return false;
			}
			latest.setDescription(editedDesc);
			changeLog.append("Edited description. \n");
		}
		return true;
	}
	
	private void mergeParents(TurboIssue original, TurboIssue latest, StringBuilder changeLog){
		ObservableList<Integer> originalParents = original.getParents();
		ObservableList<Integer> editedParents = this.getParents();
		
		HashMap<String, HashSet<Integer>> changeSet = getChangesToList(originalParents, editedParents);
		ObservableList<Integer> latestParents = latest.getParents();
		HashSet<Integer> removed = changeSet.get(REMOVED_TAG);
		HashSet<Integer> added = changeSet.get(ADDED_TAG);
		latestParents.removeAll(removed);
		for(Integer label: added){
			if(!latestParents.contains(label)){
				latestParents.add(label);
			}
		}
		latest.setParents(latestParents);
		logParentChange(removed, added, changeLog);
	}
	
	private void logParentChange(HashSet<Integer> removed, HashSet<Integer> added, StringBuilder changeLog){
		if(added.size() > 0){
			changeLog.append("Added Parents: " + added.toString() + "\n");
		}
		if(removed.size() > 0){
			changeLog.append("Removed Parents: " + removed.toString() + "\n");
		}
	}

	private void mergeTitle(TurboIssue original, TurboIssue latest, StringBuilder changeLog) {
		String originalTitle = original.getTitle();
		String editedTitle = this.getTitle();
		if (!editedTitle.equals(originalTitle)) {
			latest.setTitle(editedTitle);
			changeLog.append("Edited title \n");
		}
	}
	
	/*
	 * Private Methods
	 */
	
	private String extractDescription(String issueBody) {
		if (issueBody == null) return "";
		String description = issueBody.replaceAll(REGEX_REPLACE_DESC, "").trim();
		return description;
	}
	
	private ObservableList<TurboLabel> translateLabels(List<Label> labels) {
		ObservableList<TurboLabel> turboLabels = FXCollections.observableArrayList();
		if (labels == null) return turboLabels;
		for (Label label : labels) {
			turboLabels.add(new TurboLabel(label));
		}
		return turboLabels;
	}

	private ObservableList<Integer> extractParentNumbers(String issueBody) {
		ObservableList<Integer> parents = FXCollections.observableArrayList();
		if (issueBody == null) return parents;
		String[] lines = issueBody.split(REGEX_SPLIT_LINES);
		int seperatorLineIndex = getSeperatorIndex(lines);
		for (int i = 0; i < seperatorLineIndex; i++) {
			String line = lines[i];
			if (line.startsWith(METADATA_HEADER_PARENT)) {
				String value = line.replace(METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (int j = 0; j < valueTokens.length; j++) {
					if (valueTokens[j].trim().isEmpty()) continue;
					parents.add(Integer.parseInt(valueTokens[j].trim()));
				}
			}
		}
		return parents;
	}
	
	private int getSeperatorIndex(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].equals(METADATA_SEPERATOR)) {
				return i;
			}
		}
		return -1;
	}
	
	private String buildBody() {
		StringBuilder body = new StringBuilder();
		
		if (!parents.isEmpty()) {
			String parentsMd = METADATA_HEADER_PARENT;
			Iterator<Integer> parentsItr = parents.iterator();
			while (parentsItr.hasNext()) {
				parentsMd = parentsMd + "#" + parentsItr.next();
				if (parentsItr.hasNext()) {
					parentsMd = parentsMd + ", ";
				}
			}
			body.append(parentsMd + "\n");
		}
		
		body.append(METADATA_SEPERATOR + "\n");
		body.append(getDescription());
		return body.toString();
	}

	/*
	 * Overridden Methods
	 */
	
	@Override
	public String toString() {
		return "Issue " + getTitle();
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
