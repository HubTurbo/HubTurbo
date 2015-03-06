package model;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;
import org.ocpsoft.prettytime.PrettyTime;

import service.IssueEventType;
import service.ServiceManager;
import service.TurboIssueEvent;
import storage.DataManager;
import util.CollectionUtilities;
import util.Utility;


@SuppressWarnings("unused")
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
	private static final String NEW_LINE = "\n";
	private static final int REFRESH_FEED_MINUTES = 15;

	private void ______SERIALISED_FIELDS______() {
	}

	private String creator;
	private String createdAt;
	private LocalDateTime updatedAt;
	private int commentCount;
	private PullRequest pullRequest;
	private int id = 0;
	private String title = "";
	private String description = "";

	private int parentIssue = 0;
	private boolean state = false;
	private TurboUser assignee;
	private TurboMilestone milestone;
	private String htmlUrl;
	private ObservableList<TurboLabel> labels = FXCollections.observableArrayList();

	private void ______MISCELLANEOUS_FIELDS______() {
	}

	private WeakReference<Model> model;

	private List<TurboIssueEvent> issueFeeds = new ArrayList<TurboIssueEvent>();

	private List<Comment> comments = new ArrayList<>();
	private boolean hasNewComments = false;

	private LocalDateTime lastModifiedTime;

	private void ______ESSENTIALS______() {
	}

	public TurboIssue(String title, String desc, Model model) {
		assert title != null;
		assert desc != null;
		assert model != null;
		this.model = new WeakReference<Model>(model);

		setTitle(title);
		setDescription(desc);
		setOpen(true);
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
		setCommentCount(issue.getComments());
		setCreator(issue.getUser().getLogin());
		setCreatedAt(new SimpleDateFormat("d MMM yy, h:mm a").format(issue.getCreatedAt()));
		setUpdatedAt(LocalDateTime.ofInstant(issue.getUpdatedAt().toInstant(), ZoneId.systemDefault()));
	}

	public Issue toGhResource() {
		Issue ghIssue = new Issue();
		ghIssue.setHtmlUrl(getHtmlUrl());
		ghIssue.setNumber(getId());
		ghIssue.setTitle(getTitle());
		ghIssue.setState(isOpen() ? STATE_OPEN : STATE_CLOSED);
		if (assignee != null)
			ghIssue.setAssignee(assignee.toGhResource());
		if (milestone != null)
			ghIssue.setMilestone(milestone.toGhResource());
		ghIssue.setLabels(TurboLabel.toGhLabels(labels));
		ghIssue.setBody(buildGithubBody());
		return ghIssue;
	}

	public TurboIssue(TurboIssue other) {
		assert other != null;
		copyValues(other);
	}

	private void log(String field, String change) {
	    logger.info(String.format("Issue %d %s: %s", this.getId(), field, change));
	}

	private void log(String field, String before, String after) {
	    logger.info(String.format("Issue %d %s: '%s' -> '%s'", this.getId(), field, before, after));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void copyValues(Object other) {
	    assert other != null;
	    if (other.getClass() == TurboIssue.class) {
	        TurboIssue otherIssue = (TurboIssue)other;
	        model = otherIssue.model;

	        setHtmlUrl(otherIssue.getHtmlUrl());

	        // Logging is done with the assumption that this method is used for
	        // updating the values of TurboIssue in mind
	        if (!otherIssue.getTitle().equals(this.getTitle())) {
	            log("title", this.getTitle(), otherIssue.getTitle());
	        }
	        setTitle(otherIssue.getTitle());
	        setOpen(otherIssue.isOpen());
	        setId(otherIssue.getId());

	        if (otherIssue.getDescription() != null && this.getDescription() != null) {
	            if (!otherIssue.getDescription().equals(this.getDescription())) {
	                log("desc", this.getDescription(), otherIssue.getDescription());
	            }
	        } else if (otherIssue.getDescription() == null && this.getDescription() != null) {
	            log("desc", "removed");
	        } else if (otherIssue.getDescription() != null
	                && this.getDescription() == null) {
	            log("desc", "added");
	        }
	        setDescription(otherIssue.getDescription());

	        if (otherIssue.getAssignee() != null && this.getAssignee() != null) {
	            if (!otherIssue.getAssignee().equals(this.getAssignee())) {
	                log("assignee", this.getAssignee().logString(), otherIssue.getAssignee().logString());
	            }
	        } else if (otherIssue.getAssignee() == null && this.getAssignee() != null) {
	            log("assignee", "removed");
	        } else if (otherIssue.getAssignee() != null && this.getAssignee() == null) {
	            log("assignee", "added");
	        }
	        setAssignee(otherIssue.getAssignee());

	        if (otherIssue.getMilestone() != null && this.getMilestone() != null) {
	            if (!otherIssue.getMilestone().equals(this.getMilestone())) {
	                log("milestone", this.getMilestone().logString(), otherIssue.getMilestone().logString());
	            }
	        } else if (otherIssue.getMilestone() == null && this.getMilestone() != null) {
	            log("milestone", "removed");
	        } else if (otherIssue.getMilestone() != null && this.getMilestone() == null) {
	            log("milestone", "added");
	        }
	        setMilestone(otherIssue.getMilestone());

	        List oldList = new ArrayList<TurboLabel>();
	        List newList = new ArrayList<TurboLabel>();
	        for (TurboLabel label : this.getLabels()) {
	            oldList.add(label);
	        }
	        for (TurboLabel label : otherIssue.getLabels()) {
	            newList.add(label);
	        }
	        HashMap<String, HashSet> changes = CollectionUtilities
	                .getChangesToList(oldList, newList);
	        HashSet<TurboLabel> removed = changes
	                .get(CollectionUtilities.REMOVED_TAG);
	        HashSet<TurboLabel> added = changes
	                .get(CollectionUtilities.ADDED_TAG);
	        if (removed.size() > 0) {
	            logger.info(String.format("Issue %d labels removed: %s", this.getId(), Utility.stringify(removed)));
	        }
	        if (added.size() > 0) {
	            logger.info(String.format("Issue %d labels added: %s", this.getId(), Utility.stringify(added)));
	        }
	        setLabels(otherIssue.getLabels());
	        setParentIssue(otherIssue.getParentIssue());
	        setPullRequest(otherIssue.getPullRequest());
	        setCommentCount(otherIssue.getCommentCount());
	        setCreator(otherIssue.getCreator());
	        setCreatedAt(otherIssue.getCreatedAt());
	        setUpdatedAt(otherIssue.getUpdatedAt());
	    }
	}

	@Override
	public String toString() {
	    return "TurboIssue [id=" + id + ", title=" + title + "]";
	}

	/**
	 * A convenient string representation of this object, for purposes of readable logs.
	 * @return
	 */
	public String logString() {
	    return "Issue #" + getId() + ": " + getTitle();
	}

	@Override
	public String getListName() {
		return "#" + getId() + " " + getTitle();
	}

	private void ______UTILITY_METHODS______() {
	}

	public boolean isPullRequest() {
		return pullRequest != null && pullRequest.getUrl() != null;
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
			if (current.getParentIssue() == index)
				return true;
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

	private TurboLabel getLabelReference(TurboLabel label) {
		List<TurboLabel> allLabels = model.get().getLabels();
		int index = allLabels.indexOf(label);
		assert index != -1;
		if (index >= 0) {
			return allLabels.get(index);
		} else {
			// Should not happen
			return label;
		}
	}

	private TurboMilestone getMilestoneReference(TurboMilestone milestone) {
		List<TurboMilestone> allMilestones = model.get().getMilestones();
		int index = allMilestones.indexOf(milestone);
		if (index != -1) {
			return allMilestones.get(index);
		} else {
			return milestone;
		}
	}

	private TurboUser getCollaboratorReference(TurboUser user) {
		List<TurboUser> allCollaborators = model.get().getCollaborators();
		int index = allCollaborators.indexOf(user);
		if (index != -1) {
			return allCollaborators.get(index);
		} else {
			return user;
		}
	}

	public boolean hasLabel(TurboLabel label) {
		return labels.contains(label);
	}

	public void addLabel(TurboLabel label) {
		if (labels.contains(label)) {
			return;
		}
		if (label.isExclusive()) {
			removeLabelsWithGroup(label.getGroup());
		}

		addToLabels(label);
	}

	private void removeLabelsWithGroup(String group) {
		List<TurboLabel> labels = getLabelsWithGroup(group);
		removeLabels(labels);
	}

	private List<TurboLabel> getLabelsWithGroup(String group) {
		return labels.stream().filter(label -> group.equalsIgnoreCase(label.getGroup())).collect(Collectors.toList());
	}

	public void addLabels(List<TurboLabel> labList) {
		for (TurboLabel label : labList) {
			addLabel(label);
		}
	}

	public void removeLabel(TurboLabel label) {
		if (!labels.remove(label)) {
			return;
		}
	}

	public void removeLabels(List<TurboLabel> labList) {
		for (TurboLabel label : labList) {
			removeLabel(label);
		}
	}

	private void addToLabels(TurboLabel label) {
		labels.add(getLabelReference(label));
	}

	public static String extractDescription(String issueBody) {
		if (issueBody == null)
			return "";
		String description = issueBody.replaceAll(REGEX_REPLACE_DESC, "").trim();
		return description;
	}

	public static int extractIssueParent(String issueBody) {
		if (issueBody == null) {
			return -1;
		}
		String[] lines = issueBody.split(REGEX_SPLIT_LINES);
		int seperatorLineIndex = getSeparatorIndex(lines);
		for (int i = 0; i < seperatorLineIndex; i++) {
			String line = lines[i];

			if (line.startsWith(METADATA_HEADER_PARENT)) {
				String value = line.replace(METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (int j = 0; j < valueTokens.length; j++) {
					if (!valueTokens[j].trim().isEmpty()) {
						return Integer.parseInt(valueTokens[j].trim());
					}
				}
			} else if (line.startsWith(OLD_METADATA_HEADER_PARENT)) {
				// legacy
				String value = line.replace(OLD_METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (int j = 0; j < valueTokens.length; j++) {
					if (!valueTokens[j].trim().isEmpty()) {
						return Integer.parseInt(valueTokens[j].trim());
					}
				}
			}
		}
		return -1;
	}

	public String getFeed(int hours) {
		return "<activity feed>";
	}

	private List<TurboIssueEvent> getGitHubEvents() {
		List<TurboIssueEvent> feeds = new ArrayList<TurboIssueEvent>();
		try {
			feeds = ServiceManager.getInstance().getEvents(getId());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return feeds;
	}

	private ObservableList<TurboLabel> translateLabels(List<Label> labels) {
		ObservableList<TurboLabel> turboLabels = FXCollections.observableArrayList();
		if (labels == null)
			return turboLabels;

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
		if (parent > 0) {
			String parentData = METADATA_HEADER_PARENT + METADATA_PARENT;
			body.append(String.format(parentData, this.getParentIssue()));
			body.append(METADATA_SEPERATOR + "\n");
		}
		body.append(getDescription());
		return body.toString();
	}
	
	public boolean hasComments() {
		return comments.size() > 0;
	}
	
	public List<Comment> getComments() {
		return comments;
	}
	
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	private void ______GETTERS_AND_SETTERS______() {
	}

	public String getCreator() {
		String name = DataManager.getInstance().getUserAlias(creator);
		if (name == null) {
			name = creator;
		}
		return name;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int num) {
		this.commentCount = num;
	}

	public PullRequest getPullRequest() {
		return pullRequest;
	}

	public void setPullRequest(PullRequest pr) {
		this.pullRequest = pr;
	}

	public final int getId() {
		return id;
	}

	public final void setId(int value) {
		id = value;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String value) {
		title = value;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String value) {
		description = value;
	}

	public int getParentIssue() {
		return parentIssue;
	}

	public final void setParentIssue(int parent) {
		parentIssue = parent;
	}

	public final boolean isOpen() {
		return state;
	}

	public final void setOpen(boolean value) {
		state = value;
	}

	public TurboUser getAssignee() {
		return assignee;
	}

	public void setAssignee(TurboUser assignee) {
		this.assignee = getCollaboratorReference(assignee);
	}

	public TurboMilestone getMilestone() {
		return milestone;
	}

	public void setMilestone(TurboMilestone milestone) {
		this.milestone = getMilestoneReference(milestone);
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public ObservableList<TurboLabel> getLabels() {
		return FXCollections.observableArrayList(labels);
	}

	public void setLabels(List<TurboLabel> labels) {
		if (this.labels != labels) {
			this.labels.clear();
			for (TurboLabel label : labels) {
				addLabel(label);
			}
		}
	}
}