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
	
	private static final String STATE_CLOSED = "closed";
	private static final String STATE_OPEN = "open";
	private static final String REGEX_REPLACE_DESC = "^[^<>]*<hr>";
	private static final String REGEX_SPLIT_PARENT = "(,\\s+)?#";
	private static final String REGEX_SPLIT_LINES = "(\\r?\\n)+";
	private static final String METADATA_HEADER_PARENT = "* Parent(s): ";
	private static final String METADATA_SEPERATOR = "<hr>";
	
	/*
	 * Attributes, Getters & Setters
	 */
	
	private IntegerProperty id = new SimpleIntegerProperty();
    public final int getId() {return id.get();}
    private final void setId(int value) {id.set(value);}
    public IntegerProperty idProperty() {return id;}
	
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
    
    private TurboUser assignee;
    public TurboUser getAssignee() {return assignee;}
	public void setAssignee(TurboUser assignee) {this.assignee = assignee;}
	
	private TurboMilestone milestone;
	public TurboMilestone getMilestone() {return milestone;}
	public void setMilestone(TurboMilestone milestone) {this.milestone = milestone;}
	
	private ObservableList<TurboLabel> labels;
	public ObservableList<TurboLabel> getLabels() {return labels;}
	public void setLabels(ObservableList<TurboLabel> labels) {
		if (this.labels == null) {
			this.labels = labels;
		} else if (labels != this.labels) {
			this.labels.clear();
			this.labels.addAll(labels);
		}	
	}
	
	private ObservableList<Integer> parents;
	public ObservableList<Integer> getParents() {return parents;}
	public void setParents(ObservableList<Integer> parents) {
		if (this.parents == null) {
			this.parents = parents;
		} else if (parents != this.parents) {
			this.parents.clear();
			this.parents.addAll(parents);
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
	}
	
	// Copy constructor
	public TurboIssue(TurboIssue other) {
		assert other != null;
		
		setTitle(other.getTitle());
		setDescription(other.getDescription());
		setOpen(other.getOpen());
		setId(other.getId());
		setLabels(FXCollections.observableArrayList(other.getLabels()));
		setAssignee(other.getAssignee());
		setMilestone(other.getMilestone());
		setParents(FXCollections.observableArrayList(other.getParents()));
	}
	
	public TurboIssue(Issue issue) {
		assert issue != null;
		
		setTitle(issue.getTitle());
		setOpen(new Boolean(issue.getState().equals(STATE_OPEN)));
		setId(issue.getNumber());
		setDescription(extractDescription(issue.getBody()));
		
		this.assignee = issue.getAssignee() == null ? null : new TurboUser(issue.getAssignee());
		this.milestone = issue.getMilestone() == null ? null : new TurboMilestone(issue.getMilestone());
		this.labels = translateLabels(issue.getLabels());
		this.parents = extractParents(issue.getBody());
	}

	public Issue toGhResource() {
		Issue ghIssue = new Issue();
		ghIssue.setTitle(getTitle());
		ghIssue.setState(getOpen() ? STATE_OPEN : STATE_CLOSED);
		if (assignee != null) ghIssue.setAssignee(assignee.toGhResource());
		if (milestone != null) ghIssue.setMilestone(milestone.toGhResource());
		ghIssue.setLabels(TurboLabel.toGhLabels(labels));
		ghIssue.setBody(buildBody());
		return ghIssue;
	}
	
	/*
	 * Private Methods
	 */
	
	private String extractDescription(String issueBody) {
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

	private ObservableList<Integer> extractParents(String body) {
		ObservableList<Integer> parents = FXCollections.observableArrayList();
		String[] lines = body.split(REGEX_SPLIT_LINES);
		int seperatorLineIndex = getSeperatorIndex(lines);
		for (int i = 0; i < seperatorLineIndex; i++) {
			String line = lines[i];
			if (line.startsWith(METADATA_HEADER_PARENT)) {
				String value = line.replace(METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (int j = 0; j < valueTokens.length; j++) {
					if (valueTokens[j].trim().isEmpty()) continue;
					parents.add(Integer.parseInt(valueTokens[j]));
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
		String parentsMd = METADATA_HEADER_PARENT;
		Iterator<Integer> parentsItr = parents.iterator();
		while (parentsItr.hasNext()) {
			parentsMd = parentsMd + "#" + parentsItr.next();
			if (parentsItr.hasNext()) {
				parentsMd = parentsMd + ", ";
			}
		}
		
		StringBuilder body = new StringBuilder();
		body.append(parentsMd + "\n");
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
