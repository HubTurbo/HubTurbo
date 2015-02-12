package model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;
import org.ocpsoft.prettytime.PrettyTime;

import service.IssueEventType;
import service.ServiceManager;
import service.TurboIssueEvent;
import storage.DataManager;
import util.CollectionUtilities;

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

	private String activityFeed = "";

	private List<TurboIssueEvent> issueFeeds = new ArrayList<TurboIssueEvent>();
	private boolean hasAddedFeeds = false;
	private LocalDateTime lastModifiedTime;
	private String previousActor;
	private String previousPTime;
	private String previousMessage;
	private String currentActor;
	private String currentPTime;
	private String currentMessage;

	private ArrayList<String> labelsAdded = new ArrayList<String>();
	private ArrayList<String> labelsRemoved = new ArrayList<String>();
	private ArrayList<String> milestonesAdded = new ArrayList<String>();
	private ArrayList<String> milestonesRemoved = new ArrayList<String>();

	private int labeledCount = 0;
	private int unlabeledCount = 0;
	private int milestonedCount = 0;
	private int demilestonedCount = 0;

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void copyValues(Object other) {
		assert other != null;
		if (other.getClass() == TurboIssue.class) {
			TurboIssue obj = (TurboIssue) other;
			model = obj.model;

			setHtmlUrl(obj.getHtmlUrl());

			// Logging is done with the assumption that this method is used for
			// updating the values of TurboIssue in mind
			if (!obj.getTitle().equals(this.getTitle())) {
				logger.info("Issue " + this.getId() + "; Title was '" + this.getTitle() + "'. Now it's '"
						+ obj.getTitle() + "'");
			}
			setTitle(obj.getTitle());
			setOpen(obj.isOpen());
			setId(obj.getId());

			if (obj.getDescription() != null && this.getDescription() != null) {
				if (!obj.getDescription().equals(this.getDescription())) {
					logger.info("Issue " + this.getId() + "; Description was '" + this.getDescription()
							+ "'. Now it's '" + obj.getDescription() + "'");
				}
			} else if (obj.getDescription() == null && this.getDescription() != null) {
				logger.info("Description was removed for Issue " + this.getId());
			} else if (obj.getDescription() != null && this.getDescription() == null) {
				logger.info("Description was added for Issue " + this.getId());
			}
			setDescription(obj.getDescription());

			if (obj.getAssignee() != null && this.getAssignee() != null) {
				if (!obj.getAssignee().equals(this.getAssignee())) {
					logger.info("Issue " + this.getId() + "; Assignee was '" + this.getAssignee() + "'. Now it's '"
							+ obj.getAssignee() + "'");
				}
			} else if (obj.getAssignee() == null && this.getAssignee() != null) {
				logger.info("Assignee was removed for Issue " + this.getId());
			} else if (obj.getAssignee() != null && this.getAssignee() == null) {
				logger.info("Assignee was added for Issue " + this.getId());
			}
			setAssignee(obj.getAssignee());

			if (obj.getMilestone() != null && this.getMilestone() != null) {
				if (!obj.getMilestone().equals(this.getMilestone())) {
					logger.info("Issue " + this.getId() + "; Milestone was '" + this.getMilestone() + "'. Now it's '"
							+ obj.getMilestone() + "'");
				}
			} else if (obj.getMilestone() == null && this.getMilestone() != null) {
				logger.info("Milestone was removed for Issue " + this.getId());
			} else if (obj.getMilestone() != null && this.getMilestone() == null) {
				logger.info("Milestone was added for Issue " + this.getId());
			}
			setMilestone(obj.getMilestone());

			List oldList = new ArrayList<TurboLabel>();
			List newList = new ArrayList<TurboLabel>();
			for (TurboLabel label : this.getLabels()) {
				oldList.add(label);
			}
			for (TurboLabel label : obj.getLabels()) {
				newList.add(label);
			}
			HashMap<String, HashSet> changes = CollectionUtilities.getChangesToList(oldList, newList);
			HashSet<TurboLabel> removed = changes.get(CollectionUtilities.REMOVED_TAG);
			HashSet<TurboLabel> added = changes.get(CollectionUtilities.ADDED_TAG);
			if (removed.size() > 0) {
				logger.info(removed.size() + " label(s) removed. Label name(s):");
				removed.forEach(System.out::println);
			}
			if (added.size() > 0) {
				logger.info(added.size() + " label(s) added. Label name(s):");
				added.forEach(System.out::println);
			}
			setLabels(obj.getLabels());
			setParentIssue(obj.getParentIssue());
			setPullRequest(obj.getPullRequest());
			setCommentCount(obj.getCommentCount());
			setCreator(obj.getCreator());
			setCreatedAt(obj.getCreatedAt());
			setUpdatedAt(obj.getUpdatedAt());
		}
	}

	@Override
	public String toString() {
		return "Issue " + getTitle();
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

	public final String getActivityFeed() {
		return activityFeed;
	}

	public final void setActivityFeed(String value, LocalDateTime time) {
		lastModifiedTime = time;
		activityFeed = value;
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

	public String getFeeds(int hours, int minutes, int seconds) {
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime cutoffTime;
		if (!hasAddedFeeds) {
			cutoffTime = currentTime.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds);
			if (cutoffTime.isAfter(getUpdatedAt())) {
				// No activity feed to display
				setActivityFeed("", currentTime);
			} else {
				issueFeeds.clear();
				issueFeeds.addAll(getGithubFeed());
				setActivityFeed(formatFeeds(hours, minutes, seconds), currentTime);
			}
			hasAddedFeeds = true;
		} else {
			cutoffTime = lastModifiedTime;
			if (cutoffTime.isAfter(getUpdatedAt())) {
				// No new activity for this issue
				LocalDateTime refreshTime = currentTime.minusMinutes(REFRESH_FEED_MINUTES);
				if (refreshTime.isAfter(lastModifiedTime)) {
					// Check to update pretty time
					setActivityFeed(formatFeeds(hours, minutes, seconds), currentTime);
				}
			} else {
				issueFeeds.clear();
				issueFeeds.addAll(getGithubFeed());
				setActivityFeed(formatFeeds(hours, minutes, seconds), currentTime);
			}
		}
		return getActivityFeed();
	}

	/*
	 * Private Methods
	 */

	private List<TurboIssueEvent> getGithubFeed() {
		List<TurboIssueEvent> feeds = new ArrayList<TurboIssueEvent>();
		try {
			feeds = ServiceManager.getInstance().getFeeds(getId());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return feeds;
	}

	private String formatFeeds(int hours, int minutes, int seconds) {
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime cutoffTime = currentTime.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds);
		ArrayList<String> feedMessages = new ArrayList<String>();
		ArrayList<String> tempMessages = new ArrayList<String>();
		ArrayList<IssueEventType> eventSeq = new ArrayList<IssueEventType>();

		previousActor = "";
		previousPTime = "";
		previousMessage = "";
		currentMessage = "";
		for (TurboIssueEvent event : issueFeeds) {
			if (LocalDateTime.ofInstant(event.getDate().toInstant(), ZoneId.systemDefault()).isAfter(cutoffTime)) {
				if (isNewEvent(event)) {
					feedMessages.addAll(outputToBuffer(tempMessages, eventSeq));
					tempMessages.clear();
					eventSeq.clear();
				}
				eventSeq.add(event.getType());
				currentMessage = formatMessage(currentActor, currentPTime, event);
				if (currentMessage != null && !currentMessage.isEmpty() && !currentMessage.equals(previousMessage)) {
					tempMessages.add(currentMessage);
					previousMessage = currentMessage;
				}
			}
		}
		feedMessages.addAll(outputToBuffer(tempMessages, eventSeq));
		return outputReverseOrder(feedMessages);
	}

	private ArrayList<String> outputToBuffer(ArrayList<String> inputBuffer, ArrayList<IssueEventType> inputSeq) {
		ArrayList<String> outputBuffer = new ArrayList<String>();
		int bufferIndex = 0;

		for (IssueEventType eventType : inputSeq) {
			switch (eventType) {
			case Milestoned:
			case Demilestoned:
				if (milestonedCount > 0 || demilestonedCount > 0) {
					outputBuffer.add(aggregateMilestoneEvent());
				}
				break;
			case Labeled:
			case Unlabeled:
				if (labeledCount > 0 || unlabeledCount > 0) {
					outputBuffer.add(aggregateLabelEvent());
				}
				break;
			default:
				if (bufferIndex < inputBuffer.size()) {
					outputBuffer.add(inputBuffer.get(bufferIndex));
					++bufferIndex;
				}
			}
		}
		previousActor = currentActor;
		previousPTime = currentPTime;
		return outputBuffer;
	}

	private String outputReverseOrder(ArrayList<String> bufferList) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = bufferList.size(); i > 0; i--) {
			if (i == bufferList.size()) {
				stringBuffer.append(bufferList.get(i - 1));
			} else {
				stringBuffer.append(NEW_LINE + bufferList.get(i - 1));
			}
		}
		return stringBuffer.toString();
	}

	private boolean isNewEvent(TurboIssueEvent issueEvent) {
		PrettyTime pt = new PrettyTime();
		currentPTime = pt.format(issueEvent.getDate());
		currentActor = issueEvent.getActor().getLogin();
		if (currentActor.equalsIgnoreCase(previousActor) && currentPTime.equalsIgnoreCase(previousPTime)) {
			return false;
		} else {
			return true;
		}
	}

	private String aggregateLabelEvent() {
		String affectedLabels;
		String message;
		if (labeledCount > 0 && unlabeledCount > 0) {
			affectedLabels = labelsAdded.get(0);
			for (int i = 1; i < labelsAdded.size(); ++i) {
				affectedLabels += ", " + labelsAdded.get(i);
			}
			String removedLabels = labelsRemoved.get(0);
			for (int i = 1; i < labelsRemoved.size(); ++i) {
				removedLabels += ", " + labelsRemoved.get(i);
			}
			message = String.format("%s added %s and removed %s labels %s.", previousActor, affectedLabels,
					removedLabels, previousPTime);
		} else if (labeledCount == 1) {
			affectedLabels = labelsAdded.get(0);
			message = String.format("%s added %s label %s.", previousActor, affectedLabels, previousPTime);
		} else if (labeledCount > 1) {
			affectedLabels = labelsAdded.get(0);
			for (int i = 1; i < labelsAdded.size(); ++i) {
				affectedLabels += ", " + labelsAdded.get(i);
			}
			message = String.format("%s added %s labels %s.", previousActor, affectedLabels, previousPTime);
		} else if (unlabeledCount == 1) {
			affectedLabels = labelsRemoved.get(0);
			message = String.format("%s removed %s label %s.", previousActor, affectedLabels, previousPTime);
		} else {
			// (unlabeledCount > 1)
			affectedLabels = labelsRemoved.get(0);
			for (int i = 1; i < labelsRemoved.size(); ++i) {
				affectedLabels += ", " + labelsRemoved.get(i);
			}
			message = String.format("%s removed %s labels %s.", previousActor, affectedLabels, previousPTime);
		}
		labeledCount = 0;
		unlabeledCount = 0;
		labelsAdded.clear();
		labelsRemoved.clear();
		return message;
	}

	private String aggregateMilestoneEvent() {
		String affectedMilestones;
		String message;
		if (milestonedCount > 0 && demilestonedCount > 0) {
			affectedMilestones = milestonesAdded.get(0);
			for (int i = 1; i < milestonesAdded.size(); ++i) {
				affectedMilestones += ", " + milestonesAdded.get(i);
			}
			for (int i = 0; i < milestonesRemoved.size(); ++i) {
				affectedMilestones += ", " + milestonesRemoved.get(i);
			}
			message = String.format("%s modified the milestone: %s %s.", previousActor, affectedMilestones,
					previousPTime);
		} else if (milestonedCount > 0) {
			affectedMilestones = milestonesAdded.get(0);
			for (int i = 1; i < milestonesAdded.size(); ++i) {
				affectedMilestones += ", " + milestonesAdded.get(i);
			}
			message = String.format("%s added to %s milestone %s.", previousActor, affectedMilestones, previousPTime);
		} else {
			// (demilestonedCount > 0)
			affectedMilestones = milestonesRemoved.get(0);
			for (int i = 1; i < milestonesRemoved.size(); ++i) {
				affectedMilestones += ", " + milestonesRemoved.get(i);
			}
			message = String.format("%s removed from %s milestone %s.", previousActor, affectedMilestones,
					previousPTime);
		}
		milestonedCount = 0;
		demilestonedCount = 0;
		milestonesAdded.clear();
		milestonesRemoved.clear();
		return message;
	}

	private String formatMessage(String actorName, String timeString, TurboIssueEvent issueEvent) {
		String message = "";
		switch (issueEvent.getType()) {
		case Renamed:
			message = String.format("%s renamed this issue %s.", actorName, timeString);
			break;
		case Milestoned:
			milestonedCount++;
			milestonesAdded.add(issueEvent.getMilestoneTitle());
			break;
		case Demilestoned:
			demilestonedCount++;
			milestonesRemoved.add(issueEvent.getMilestoneTitle());
			break;
		case Labeled:
			labeledCount++;
			labelsAdded.add(issueEvent.getLabelName());
			break;
		case Unlabeled:
			unlabeledCount++;
			labelsRemoved.add(issueEvent.getLabelName());
			break;
		case Assigned:
			message = String.format("%s was assigned to this issue %s.", actorName, timeString);
			break;
		case Unassigned:
			message = String.format("%s was unassigned from this issue %s.", actorName, timeString);
			break;
		case Closed:
			message = String.format("%s closed this issue %s.", actorName, timeString);
			break;
		case Reopened:
			message = String.format("%s reopened this issue %s.", actorName, timeString);
			break;
		case Locked:
			message = String.format("%s locked issue %s.", actorName, timeString);
			break;
		case Unlocked:
			message = String.format("%s unlocked this issue %s.", actorName, timeString);
			break;
		case Referenced:
			message = String.format("%s referenced this issue %s.", actorName, timeString);
			break;
		case Subscribed:
			message = String.format("%s subscribed to receive notifications for this issue %s.", actorName, timeString);
			break;
		case Mentioned:
			message = String.format("%s was mentioned %s.", actorName, timeString);
			break;
		case Merged:
			message = String.format("%s merged this issue %s.", actorName, timeString);
			break;
		case HeadRefDeleted:
			message = String.format("%s deleted the pull request's branch %s.", actorName, timeString);
			break;
		case HeadRefRestored:
			message = String.format("%s restored the pull request's branch %s.", actorName, timeString);
			break;
		default:
			// Not yet implemented, or no events triggered
			message = String.format("%s %s %s.", actorName, issueEvent.getType().toString(), timeString);
		}
		return message;
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