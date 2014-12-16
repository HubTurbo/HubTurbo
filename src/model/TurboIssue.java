package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;

import service.ServiceManager;
import storage.DataManager;


public class TurboIssue implements Listable {
	private static final Logger logger = LogManager.getLogger(TurboIssue.class.getName());
	private static final String STATE_CLOSED = "closed";
	private static final String STATE_OPEN = "open";
	private static final String REGEX_REPLACE_DESC = "^[^<>]*<hr>";
	private static final String REGEX_SPLIT_PARENT = "(,\\s+)?#";
	private static final String REGEX_SPLIT_LINES = "(\\r?\\n)+";
	private static final String METADATA_HEADER_PARENT = "* Parent: ";
	private static final String OLD_METADATA_HEADER_PARENT = "* Parent(s): ";
	private static final String METADATA_PARENT = "#%1d \n";
	private static final String METADATA_SEPERATOR = "<hr>";
	
	/*
	 * Attributes, Getters & Setters
	 */

	private WeakReference<Model> model;
	private WeakReference<Model> getModel(){
		return model;
	}
	
	private String creator;
	public String getCreator() {
		String name = DataManager.getInstance().getUserAliases().get(creator);
		if (name == null) {
			name = creator;
		}
		return name;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	private String createdAt;
	public String getCreatedAt() {
		return this.createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	
	private int numOfComments;
	public int getNumOfComments(){
		return numOfComments;
	}
	public void setNumOfComments(int num){
		this.numOfComments = num;
	}
	
	private PullRequest pullRequest;
	public PullRequest getPullRequest(){
		return pullRequest;
	}
	public void setPullRequest(PullRequest pr){
		this.pullRequest = pr;
	}
	
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
    	cachedDescriptionMarkup = null; //markup is invalid since the issue's description is to be overwritten
    	description.set(value);
    }
    public StringProperty descriptionProperty() {
    	return description;
    }
    
    private String cachedDescriptionMarkup;
    public String getDescriptionMarkup(){
    	try{
    		if(cachedDescriptionMarkup == null){
        		final String desc = getDescription();
        		cachedDescriptionMarkup = ServiceManager.getInstance().getContentMarkup(desc);
        	}
    	}catch(IOException e){
    		logger.error(e.getLocalizedMessage(), e);
    		return getDescription();
    	}
    	return cachedDescriptionMarkup;
    }
    public void setDescriptionMarkup(String descMarkup) {
    	this.cachedDescriptionMarkup = descMarkup;
    }
    
    private IntegerProperty parentIssue = new SimpleIntegerProperty();
    public int getParentIssue(){
    	return parentIssue.get();
    }
    public final IntegerProperty parentIssueProperty(){
    	return parentIssue;
    }
    public final void setParentIssue(int parent){
    	parentIssue.set(parent);
    }
    public final TurboIssue parentReference() {
    	if (getParentIssue() != -1) {
    		return model.get().getIssueWithId(getParentIssue());
    	}
    	return null;
    }
    public boolean hasAncestor(int index) {
    	TurboIssue current = this;
		while (current.getParentIssue() != -1) {
			if (current.getParentIssue() == index) return true;
			current = current.parentReference();
		}
    	return false;
    }
    public int getDepth() {
    	int depth = 0;
    	TurboIssue current = this;
		while (current.getParentIssue() != -1) {
			++depth;
			current = current.parentReference();
		}
    	return depth;
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
		this.assignee = getCollaboratorReference(assignee);
	}
	
	private TurboMilestone milestone;
	public TurboMilestone getMilestone() {
		return milestone;
	}
	public void setMilestone(TurboMilestone milestone) {
		this.milestone = getMilestoneReference(milestone);
	}
	
	private String htmlUrl;
    public String getHtmlUrl() {
    	return htmlUrl;
    }
	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}
	
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();
	public ObservableList<TurboLabel> getLabels() {
		return FXCollections.observableArrayList(labels);
	}
	public ObservableList<TurboLabel> getLabelsReference() {
		return labels;
	}
	
	private TurboLabel getLabelReference(TurboLabel label){
		List<TurboLabel> allLabels = model.get().getLabels();
		int index = allLabels.indexOf(label);
		assert index != -1;
		if(index >= 0){
			return allLabels.get(index);
		}else{
			//Should not happen
			return label;
		}
	}
	
	private TurboMilestone getMilestoneReference(TurboMilestone milestone){
		List<TurboMilestone> allMilestones = model.get().getMilestones();
		int index = allMilestones.indexOf(milestone);
		if(index != -1){
			return allMilestones.get(index);
		}else{
			return milestone;
		}
	}
	
	private TurboUser getCollaboratorReference(TurboUser user){
		List<TurboUser> allCollaborators = model.get().getCollaborators();
		int index = allCollaborators.indexOf(user);
		if(index != -1){
			return allCollaborators.get(index);
		}else{
			return user;
		}
	}
	
	public boolean hasStatusLabel(){
		for(TurboLabel label : labels){
			String ghName = label.toGhName();
			if(DataManager.getInstance().isStatusLabel(ghName)){
				return true;
			}
		}
		return false;
	}
	
	public TurboLabel getStatusLabel(){
		List<TurboLabel> statusLabel = labels.stream()
				.filter(l -> (DataManager.getInstance().isStatusLabel(l.toGhName())))
				.collect(Collectors.toList());
		if(!statusLabel.isEmpty()){
			return statusLabel.get(0);
		}else{
			return null;
		}
	}
	
	public List<TurboLabel> getNonStatusLabel(){
		return labels.stream()
				.filter(l -> (!DataManager.getInstance().isStatusLabel(l.toGhName())))
				.collect(Collectors.toList());
	}
	
	public boolean hasLabel(TurboLabel label){
		return labels.contains(label);
	}
	
	public void addLabel(TurboLabel label){
		if(labels.contains(label)){
			return;
		}
		if(label.isExclusive()){
			removeLabelsWithGroup(label.getGroup());
		}
		if (DataManager.getInstance().isClosedStatusLabel(label.toGhName())) {
			this.setOpen(false);
		}else if(DataManager.getInstance().isOpenStatusLabel(label.toGhName())){
			this.setOpen(true);
		}
		addToLabels(label);
	}
	
	private void removeLabelsWithGroup(String group){
		List<TurboLabel> labels = getLabelsWithGroup(group);
		removeLabels(labels);
	}
	
	private List<TurboLabel> getLabelsWithGroup(String group){
		return labels.stream()
				.filter(label -> group.equalsIgnoreCase(label.getGroup()))
				.collect(Collectors.toList());
	}
	
	public void addLabels(List<TurboLabel> labList){
		for(TurboLabel label : labList){
			addLabel(label);
		}
	}
	
	public void removeLabel(TurboLabel label){
		if(!labels.remove(label)){
			return;
		}
		if (DataManager.getInstance().isClosedStatusLabel(label.toGhName())) {
			//Default status of the issue is open
			this.setOpen(true);
		}
	}
	
	public void removeLabels(List<TurboLabel> labList){
		for(TurboLabel label : labList){
			removeLabel(label);
		}
	}
	
	private void addToLabels(TurboLabel label){
		labels.add(getLabelReference(label));
	}
	
	public void setLabels(List<TurboLabel> labels) {
		if(this.labels != labels){
			clearAllLabels();
			for(TurboLabel label : labels){
				addLabel(label);
			}
		}
	}
	
	public void clearAllLabels(){
		if(this.hasStatusLabel()){
			this.setOpen(true);
		}
		this.labels.clear();
	}

	/*
	 * Constructors & Public Methods
	 */
	
	public TurboIssue(String title, String desc, Model model) {
		assert title != null;
		assert desc != null;
		assert model != null;
		this.model = new WeakReference<Model>(model);
		
		setTitle(title);
		setDescription(desc);
		setOpen(true);
	}
	
	// Copy constructor
	public TurboIssue(TurboIssue other) {
		assert other != null;
		copyValues(other);	
	}
	
	public TurboIssue(Issue issue, Model model) {
		assert issue != null;
		assert model != null;
		this.model = new WeakReference<Model>(model);
		setHtmlUrl(issue.getHtmlUrl());
		setTitle(issue.getTitle());
		setOpen(new Boolean(issue.getState().equals(STATE_OPEN)));
		setId(issue.getNumber());
		setDescription(extractDescription(issue.getBody()));
		setAssignee(issue.getAssignee() == null ? null : new TurboUser(issue.getAssignee()));
		setMilestone(issue.getMilestone() == null ? null : new TurboMilestone(issue.getMilestone()));
		setLabels(translateLabels(issue.getLabels()));
		setParentIssue(extractIssueParent(issue.getBody()));
		setPullRequest(issue.getPullRequest());
		setNumOfComments(issue.getComments());
		setCreator(issue.getUser().getLogin());
		setCreatedAt(new SimpleDateFormat("d MMM yy, h:mm a").format(issue.getCreatedAt()));
	}

	public Issue toGhResource() {
		Issue ghIssue = new Issue();
		ghIssue.setHtmlUrl(getHtmlUrl());
		ghIssue.setNumber(getId());
		ghIssue.setTitle(getTitle());
		ghIssue.setState(getOpen() ? STATE_OPEN : STATE_CLOSED);
		if (assignee != null) ghIssue.setAssignee(assignee.toGhResource());
		if (milestone != null) ghIssue.setMilestone(milestone.toGhResource());
		ghIssue.setLabels(TurboLabel.toGhLabels(labels));
		ghIssue.setBody(buildGithubBody());
		return ghIssue;
	}
	
	public void copyValues(Object other) {
		assert other != null;
		if(other.getClass() == TurboIssue.class){
			TurboIssue obj = (TurboIssue)other;
			model = obj.getModel();
			
			setHtmlUrl(obj.getHtmlUrl());
			setTitle(obj.getTitle());
			setOpen(obj.getOpen());
			setId(obj.getId());
			setDescription(obj.getDescription());
			setAssignee(obj.getAssignee());
			setMilestone(obj.getMilestone());
			setLabels(obj.getLabels());
			setParentIssue(obj.getParentIssue());
			setPullRequest(obj.getPullRequest());
			setNumOfComments(obj.getNumOfComments());
			setCreator(obj.getCreator());
			setCreatedAt(obj.getCreatedAt());
		}
	}
	
	public static String extractDescription(String issueBody) {
		if (issueBody == null) return "";
		String description = issueBody.replaceAll(REGEX_REPLACE_DESC, "").trim();
		return description;
	}
	
	public static Integer extractIssueParent(String issueBody) {
		if (issueBody == null){
			return  -1;
		}
		String[] lines = issueBody.split(REGEX_SPLIT_LINES);
		int seperatorLineIndex = getSeparatorIndex(lines);
		for (int i = 0; i < seperatorLineIndex; i++) {
			String line = lines[i];
			
			if (line.startsWith(METADATA_HEADER_PARENT)) {
				String value = line.replace(METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (int j = 0; j < valueTokens.length; j++) {
					if (!valueTokens[j].trim().isEmpty()){
						return Integer.parseInt(valueTokens[j].trim());
					}
				}
			}else if(line.startsWith(OLD_METADATA_HEADER_PARENT)){
				//legacy
				String value = line.replace(OLD_METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (int j = 0; j < valueTokens.length; j++) {
					if (!valueTokens[j].trim().isEmpty()){
						return Integer.parseInt(valueTokens[j].trim());
					}
				}
			}
		}
		return -1;
	}
	
	/*
	 * Private Methods
	 */
	
	private ObservableList<TurboLabel> translateLabels(List<Label> labels) {
		ObservableList<TurboLabel> turboLabels = FXCollections.observableArrayList();
		if (labels == null) return turboLabels;
		
		for (Label label : labels) {
			turboLabels.add(new TurboLabel(label));
		}
		
		return turboLabels;
	}
	
	private static int getSeparatorIndex(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].equals(METADATA_SEPERATOR)) {
				return i;
			}
		}
		return -1;
	}
	
	public String buildGithubBody() {
		StringBuilder body = new StringBuilder();
		int parent = this.getParentIssue();
		if(parent > 0){
			String parentData = METADATA_HEADER_PARENT + METADATA_PARENT;
			body.append(String.format(parentData, this.getParentIssue()));
			body.append(METADATA_SEPERATOR + "\n");
		}
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
	
	/**
	 * Deprecated methods
	 * */
//	private ObservableList<Integer> parents = FXCollections.observableArrayList();
//	public ObservableList<Integer> getParents() {
//		return  FXCollections.observableArrayList(parents);
//	}
//	public ObservableList<Integer> getParentsReference(){
//		return parents;
//	} 
//	
//	public void setParents(ObservableList<Integer> parentNumbers) {
//		if (this.parents == null) {
//			this.parents = parentNumbers;
//		} else if (parentNumbers != this.parents) {
//			this.parents.clear();
//			if(!parentNumbers.isEmpty()){
//				this.parents.add(parentNumbers.get(0));
//			}
//		}
//	}
//	
//	public void addParent(Integer parentId){
//		//Only single parent for now. This might be extended in future to allow multiple parents
//		this.parents.clear();
//		this.parents.add(parentId);
//	}
//	
//	private ObservableList<Integer> extractParentNumbers(String issueBody) {
//		ObservableList<Integer> parents = FXCollections.observableArrayList();
//		if (issueBody == null) return parents;
//		String[] lines = issueBody.split(REGEX_SPLIT_LINES);
//		int seperatorLineIndex = getSeperatorIndex(lines);
//		for (int i = 0; i < seperatorLineIndex; i++) {
//			String line = lines[i];
//			if (line.startsWith(METADATA_HEADER_PARENT)) {
//				String value = line.replace(METADATA_HEADER_PARENT, "");
//				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
//				for (int j = 0; j < valueTokens.length; j++) {
//					if (valueTokens[j].trim().isEmpty()) continue;
//					parents.add(Integer.parseInt(valueTokens[j].trim()));
//				}
//			}
//		}
//		return parents;
//	}
	
//	public String buildGithubBody() {
//		StringBuilder body = new StringBuilder();
//
//		if (!parents.isEmpty()) {
//			String parentsMd = METADATA_HEADER_PARENT;
//			Iterator<Integer> parentsItr = parents.iterator();
//			while (parentsItr.hasNext()) {
//				parentsMd = parentsMd + "#" + parentsItr.next();
//				if (parentsItr.hasNext()) {
//					parentsMd = parentsMd + ", ";
//				}
//			}
//			body.append(parentsMd + "\n");
//		}
//
//		body.append(METADATA_SEPERATOR + "\n");
//		body.append(getDescription());
//		return body.toString();
//	}
}
