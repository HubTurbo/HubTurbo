package model;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.*;
import service.ServiceManager;
import service.TurboIssueEvent;
import storage.DataManager;
import util.CollectionUtilities;
import util.Utility;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class TurboIssue implements TurboResource {

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
	private String htmlUrl;

	/**
	 * These fields should not be accessed directly. They are lazily
	 * loaded and may not yet be available at an arbitrary point.
	 * Access them through getLabels() or getMilestone() instead.
	 */
	private Optional<TurboMilestone> milestone = Optional.empty();
	private Optional<List<TurboLabel>> labels = Optional.empty();

	/**
	 * These fields contain references to information required for lazy loading.
	 */
	private Optional<Milestone> temporaryMilestone = Optional.empty();
	private Optional<List<Label>> temporaryLabels = Optional.empty();

	private void ______MISCELLANEOUS_FIELDS______() {
	}

	private Model model;

	private List<TurboIssueEvent> events = new ArrayList<>();

	private List<Comment> comments = new ArrayList<>();
	private boolean hasNewComments = false;

	private LocalDateTime lastModifiedTime;

	private void ______ESSENTIALS______() {
	}

	public TurboIssue(String title, String desc, Model model) {
		assert title != null;
		assert desc != null;
		assert model != null;
		this.model = model;

		setTitle(title);
		setDescription(desc);
		setOpen(true);
	}

	public TurboIssue(Issue issue, Model model) {
		assert issue != null;
		assert model != null;
		this.model = model;
		setHtmlUrl(issue.getHtmlUrl());
		setTitle(issue.getTitle());
		setOpen(issue.getState().equals(STATE_OPEN));
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
			ghIssue.setMilestone(getMilestone().toGhResource());
		ghIssue.setLabels(TurboLabel.toGhLabels(getLabelCollection()));
		ghIssue.setBody(buildGithubBody());
		return ghIssue;
	}

	public TurboIssue(TurboIssue other) {
		assert other != null;
		copyValuesFrom(other);
	}

	private void log(String field, String change) {
	    logger.info(String.format("Issue %d %s: %s", this.getId(), field, change));
	}

	private void log(String field, String before, String after) {
	    logger.info(String.format("Issue %d %s: '%s' -> '%s'", this.getId(), field, before, after));
	}

	@Override
	public void copyValuesFrom(TurboResource other) {
	    assert other != null;
		assert other instanceof TurboIssue;

		TurboIssue otherIssue = (TurboIssue) other;
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

		List<TurboLabel> oldList = new ArrayList<>(this.getLabels());
		List<TurboLabel> newList = new ArrayList<>(otherIssue.getLabels());
		HashMap<String, HashSet<TurboLabel>> changes = CollectionUtilities
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

	@Override
	public String toString() {
	    return "TurboIssue [id=" + id + ", title=" + title + "]";
	}

	/**
	 * A convenient string representation of this object, for purposes of readable logs.
	 * @return a string representation suitable for logs
	 */
	public String logString() {
	    return "Issue #" + getId() + ": " + getTitle();
	}

	private void ______UTILITY_METHODS______() {
	}

	public boolean isPullRequest() {
		return pullRequest != null && pullRequest.getUrl() != null;
	}

	public final TurboIssue parentReference() {
		if (getParentIssue() != -1) {
			return model.getIssueWithId(getParentIssue());
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
		assert label != null;
		List<TurboLabel> allLabels = model.getLabels();
		int index = allLabels.indexOf(label);
		assert index != -1;
		return allLabels.get(index);
	}

	private TurboMilestone getMilestoneReference(TurboMilestone milestone) {
		assert milestone != null;
		List<TurboMilestone> allMilestones = model.getMilestones();
		int index = allMilestones.indexOf(milestone);
		assert index != -1;
		return allMilestones.get(index);
	}

	private TurboUser getCollaboratorReference(TurboUser user) {
		List<TurboUser> allCollaborators = model.getCollaborators();
		int index = allCollaborators.indexOf(user);
		if (index != -1) {
			return allCollaborators.get(index);
		} else {
			return user;
		}
	}

	public boolean hasLabel(TurboLabel label) {
		return getLabelCollection().contains(label);
	}

	public void addLabel(TurboLabel label) {
		if (getLabelCollection().contains(label)) {
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
		return getLabelCollection().stream()
			.filter(label -> group.equalsIgnoreCase(label.getGroup()))
			.collect(Collectors.toList());
	}

	public void addLabels(List<TurboLabel> newLabels) {
		newLabels.forEach(this::addLabel);
	}

	public void removeLabel(TurboLabel label) {
		getLabelCollection().remove(label);
	}

	public void removeLabels(List<TurboLabel> toRemove) {
		toRemove.forEach(this::removeLabel);
	}

	private void addToLabels(TurboLabel label) {
		getLabelCollection().add(getLabelReference(label));
	}

	public static String extractDescription(String issueBody) {
		if (issueBody == null) {
			return "";
		}
		return issueBody.replaceAll(REGEX_REPLACE_DESC, "").trim();
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
				for (String valueToken : valueTokens) {
					if (!valueToken.trim().isEmpty()) {
						return Integer.parseInt(valueToken.trim());
					}
				}
			} else if (line.startsWith(OLD_METADATA_HEADER_PARENT)) {
				// legacy
				String value = line.replace(OLD_METADATA_HEADER_PARENT, "");
				String[] valueTokens = value.split(REGEX_SPLIT_PARENT);
				for (String valueToken : valueTokens) {
					if (!valueToken.trim().isEmpty()) {
						return Integer.parseInt(valueToken.trim());
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Creates a JavaFX node containing a graphical display of this issue's events.
	 * @param withinHours the number of hours to bound the returned events by
	 * @return the node
	 */
	public Node getEventDisplay(final int withinHours) {
		final LocalDateTime now = LocalDateTime.now();

		List<TurboIssueEvent> eventsWithinDuration = events.stream()
			.filter(event -> {
				LocalDateTime eventTime = Utility.longToLocalDateTime(event.getDate().getTime());
				int hours = Utility.safeLongToInt(eventTime.until(now, ChronoUnit.HOURS));
				return hours < withinHours;
			})
			.collect(Collectors.toList());

		List<Comment> commentsWithinDuration = this.comments.stream()
			.filter(comment -> {
				LocalDateTime created = Utility.longToLocalDateTime(comment.getCreatedAt().getTime());
				int hours = Utility.safeLongToInt(created.until(now, ChronoUnit.HOURS));
				return hours < withinHours;
			})
			.collect(Collectors.toList());

		return layoutEvents(eventsWithinDuration, commentsWithinDuration);
	}

	/**
	 * Given a list of issue events, returns a JavaFX node laying them out properly.
	 * @param events
	 * @param comments
	 * @return
	 */
	private static Node layoutEvents(List<TurboIssueEvent> events, List<Comment> comments) {
		VBox result = new VBox();
		result.setSpacing(3);
		VBox.setMargin(result, new Insets(3, 0, 0, 0));

		// Events
		events.stream()
			.map(TurboIssueEvent::display)
			.forEach(e -> result.getChildren().add(e));

		// Comments
		if (comments.size() > 0) {
			String names = comments.stream()
				.map(comment -> comment.getUser().getLogin())
				.distinct()
				.collect(Collectors.joining(", "));
			HBox commentDisplay = new HBox();
			commentDisplay.getChildren().addAll(
				TurboIssueEvent.octicon(TurboIssueEvent.OCTICON_QUOTE),
				new javafx.scene.control.Label(String.format("%d comments since, involving %s.", comments.size(),
					names))
			);
			result.getChildren().add(commentDisplay);
		}

		return result;
	}

	/**
	 * Given a list of issue events, returns a textual representation of them,
	 * concatenated together with newlines.
	 * @param events
	 * @param width
	 * @return
	 */
	private static Node formatEventsText(List<TurboIssueEvent> events, int width) {
		String text = events.stream()
			.map(TurboIssueEvent::toString)
			.collect(Collectors.joining("\n"));

		Text display = new Text(text);
		display.setWrappingWidth(width);
		display.getStyleClass().add("issue-panel-feed");
		return display;
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

	private List<TurboLabel> translateLabels(List<Label> labels) {
		List<TurboLabel> turboLabels = new ArrayList<>();
		if (labels == null) {
			return turboLabels;
		}

		turboLabels.addAll(labels.stream()
			.map(label -> new TurboLabel(label))
			.collect(Collectors.toList()));

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

	public boolean hasEvents() {
		return events.size() > 0;
	}

	public List<TurboIssueEvent> getEvents() {
		return events;
	}

	public void setEvents(List<TurboIssueEvent> events) {
		this.events = events;
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

	/**
	 * Returns the pull request object associated with this issue.
	 * Is not guaranteed to be null even if the issue is not a pull request;
	 * use {@link #isPullRequest()} to check for that.
	 * @return the pull request object
	 */
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
		if (!milestone.isPresent()) {
			if (!temporaryMilestone.isPresent()) {
				return null;
			}
			milestone = Optional.of(model.getMilestoneByTitle(temporaryMilestone.get().getTitle()));
		}
		return milestone.get();
	}

	public void setMilestone(TurboMilestone milestone) {
		if (milestone == null) {
			this.milestone = Optional.empty();
		} else {
			this.milestone = Optional.of(getMilestoneReference(milestone));
		}
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public List<TurboLabel> getLabels() {
		return new ArrayList<>(getLabelCollection());
	}

	private List<TurboLabel> getLabelCollection() {
		if (!labels.isPresent()) {
			if (!temporaryLabels.isPresent()) {
				return new ArrayList<>();
			}
			List<TurboLabel> newLabels = temporaryLabels.get().stream()
				.map(label -> model.getLabelByGhName(label.getName()))
				.collect(Collectors.toList());
			labels = Optional.of(newLabels);
		}
		return labels.get();
	}

	public void setLabels(List<TurboLabel> labels) {
		this.labels = Optional.of(labels);
	}

	public void setTemporaryLabels(Optional<List<Label>> labels) {
		this.temporaryLabels = labels;
	}

	public void setTemporaryMilestone(Optional<Milestone> milestone) {
		this.temporaryMilestone = milestone;
	}
}